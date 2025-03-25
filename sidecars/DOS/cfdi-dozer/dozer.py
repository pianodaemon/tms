import os
import json
import time
import redis
import contextlib
import sys
import traceback
import logging
from pythonjsonlogger import jsonlogger

# Set up JSON logging
logHandler = logging.StreamHandler(sys.stdout)
formatter = jsonlogger.JsonFormatter('%(asctime)s %(name)s %(levelname)s %(message)s')
logHandler.setFormatter(formatter)
logger = logging.getLogger()
logger.addHandler(logHandler)
logger.setLevel(logging.DEBUG if os.getenv("DEBUG", "False").lower() == "true" else logging.INFO)

@contextlib.contextmanager
def connect_to_redis():
    """Context manager for Redis connection handling."""
    redis_host = os.getenv("REDIS_HOST")
    redis_port = os.getenv("REDIS_PORT")
    connection_str = f"redis://{redis_host}:{redis_port}/0"
    client = redis.Redis.from_url(connection_str)
    try:
        logger.debug("Connected to Redis at %s", connection_str)
        yield client
    finally:
        client.close()
        logger.debug("Redis connection closed")

class QueueNotFoundException(Exception):
    """Custom exception for missing Redis queues."""
    pass

class RedisIncSet:
    def __init__(self, set_name):
        self.set_name = set_name
        logger.info("Initialized RedisIncSet with name %s", set_name)

    def is_present(self):
        with connect_to_redis() as client:
            exists = client.exists(self.set_name) > 0
            logger.debug("Checked presence of set %s: %s", self.set_name, exists)
            return exists

    def ping(self):
        with connect_to_redis() as client:
            client.ping()
            logger.debug("Pinged Redis for set %s", self.set_name)

    def add(self, item, timestamp):
        """Add an item to the sorted set with a score (timestamp)."""
        with connect_to_redis() as client:
            client.zadd(self.set_name, {item: timestamp})
            logger.info("Added item to set %s with timestamp %d", self.set_name, timestamp)

    def fetch_due_items(self, timestamp):
        """Fetch items from sorted set that are due up to the specified timestamp."""
        with connect_to_redis() as client:
            items = client.zrangebyscore(self.set_name, 0, str(timestamp))
            logger.debug("Fetched due items from set %s up to timestamp %d", self.set_name, timestamp)
            return items

    def remove_item(self, item):
        """Remove an item from the sorted set."""
        with connect_to_redis() as client:
            client.zrem(self.set_name, item)
            logger.info("Removed item from set %s", self.set_name)

class RedisIncQueue:
    def __init__(self, queue_name):
        self.queue_name = queue_name
        logger.info("Initialized RedisIncQueue with name %s", queue_name)

    def push(self, message):
        """Push a message to the queue."""
        with connect_to_redis() as client:
            client.lpush(self.queue_name, message)
            logger.info("Pushed message to queue %s", self.queue_name)

    def pop(self, timeout: int = 0):
        """
        Remove and return the last item from the queue in a blocking manner.
        :param timeout: Timeout in seconds. If 0, waits indefinitely for an item.
        :return: The item removed from the queue, or None if timed out.
        """
        with connect_to_redis() as client:
            item = client.brpop(self.queue_name, timeout=timeout)
            logger.debug("Popped message from queue %s with timeout %d", self.queue_name, timeout)
            return item[1] if item else None

class FlowDozer:
    """Class for contention of the iteming backlog."""

    def __init__(self, input_set_name, output_queue_name, notification_queue_name):
        self.input_set = RedisIncSet(input_set_name)
        self.output_queue = RedisIncQueue(output_queue_name)
        self.notification_queue = RedisIncQueue(notification_queue_name)
        logger.info("Initialized FlowDozer with input set %s, output queue %s, and notification queue %s",
                    input_set_name, output_queue_name, notification_queue_name)

    def __call__(self, wait_for_set, interval=-1):
        # Check for the presence of the set based on WAIT_FOR_SET
        if wait_for_set:
            while not self.input_set.is_present():
                logger.warning("Input set '%s' not found. Waiting...", self.input_set.set_name)
                time.sleep(10)
        else:
            if not self.input_set.is_present():
                raise QueueNotFoundException(f"Input set '{self.input_set.set_name}' not found and WAIT_FOR_SET is disabled.")

        while True:
            current_time = int(time.time())
            scheduled_items = self.input_set.fetch_due_items(current_time)

            if scheduled_items:
                self.process_items(scheduled_items)

            if interval < 0:
                break

            time.sleep(interval)

    def process_items(self, items):
        """Process scheduled items by pushing them to the output queue and removing them from the input set."""
        for item in items:
            self.output_queue.push(item)  # Push to output queue
            self.input_set.remove_item(item)  # Remove from input set
            logger.info("Processed item %s from input set %s", item, self.input_set.set_name)

            d = json.loads(item)
            receipt = d['receipt']
            forward_event = {
                "type": "HAND_OVER_TO_PROCESSOR",
                "receipt_id": receipt['_id'],
                "timestamp": int(time.time())
            }
            forward_event_json = json.dumps(forward_event)
            self.notification_queue.push(forward_event_json)
            logger.debug("Created forward event %s and pushed to notification queue", forward_event_json)

def main():
    input_set_name = os.getenv("TIMETABLE_SET_NAME", "timeTable")
    output_queue_name = os.getenv("PROCESSOR_INPUT_QUEUE_NAME", "processorInput")
    notification_queue_name = os.getenv("NOTIFICATIONS_QUEUE_NAME", "notifications")
    poll_interval = int(os.getenv("POLL_INTERVAL", 1))
    wait_for_set = os.getenv("WAIT_FOR_SET", "false").lower() == "true"

    try:
        flow_dozer = FlowDozer(input_set_name, output_queue_name, notification_queue_name)
        flow_dozer(wait_for_set, poll_interval)
    except KeyboardInterrupt:
        logger.info("Exiting")
    except Exception as e:
        logger.critical("Critical error in processor: %s", e)
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
