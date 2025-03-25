import os
import time
import json
import unittest
from testcontainers.redis import RedisContainer
from redis import Redis
from dozer import FlowDozer, QueueNotFoundException  # Update with the actual module name


class TestFlowDozer(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Start Redis container
        cls.redis_container = RedisContainer()
        cls.redis_container.start()

        # Set Redis environment variables for the application
        cls.redis_host = cls.redis_container.get_container_host_ip()
        cls.redis_port = cls.redis_container.get_exposed_port(6379)
        os.environ["REDIS_HOST"] = cls.redis_host
        os.environ["REDIS_PORT"] = cls.redis_port

        # Initialize Redis client for direct test interactions
        cls.redis_client = Redis(host=cls.redis_host, port=int(cls.redis_port), db=0)
        cls.redis_client.flushall()  # Clear database for a fresh start

        # Define test set and queue names
        cls.notification_queue_name = "notifications"
        cls.input_set_name = "timeTable"
        cls.output_queue_name = "processorInput"

    @classmethod
    def tearDownClass(cls):
        cls.redis_client.close()
        cls.redis_container.stop()

    def setUp(self):
        # Initialize FlowDozer for each test
        self.flow_dozer = FlowDozer(self.input_set_name, self.output_queue_name, self.notification_queue_name)

    def add_message_to_set(self, message_id, timestamp):
        """Helper to add a JSON message to the input set."""
        json_message = json.dumps({"receipt":{"_id": message_id}})
        self.flow_dozer.input_set.add(json_message, timestamp)

    def verify_notification_message(self, expected_message_id):
        """Helper to verify the structure and content of the notification message."""
        notification_message = self.flow_dozer.notification_queue.pop(timeout=1)
        self.assertIsNotNone(notification_message, "Expected a message in the notifications queue.")

        notification_data = json.loads(notification_message)
        self.assertEqual(notification_data["type"], "HAND_OVER_TO_PROCESSOR", "Incorrect notification type.")
        self.assertEqual(notification_data["receipt_id"], expected_message_id, "Incorrect receipt_id in notification.")
        self.assertIsInstance(notification_data["timestamp"], int, "Timestamp should be an integer.")

    def test_flow_dozer_processing_due_items(self):
        """Test that due items are moved from the sorted set to the output queue."""
        current_timestamp = int(time.time())
        message_id = "message_1"

        # Add and process the due item
        self.add_message_to_set(message_id, current_timestamp)
        self.flow_dozer(False)  # Process one shot immediately

        # Check that due items are processed and removed
        remaining_due_items = self.flow_dozer.input_set.fetch_due_items(current_timestamp)
        self.assertEqual(len(remaining_due_items), 0, "No due items should remain in the input set")

        # Verify notification message
        self.verify_notification_message(message_id)

    def test_flow_dozer_future_items_remain_in_set_until_due(self):
        """Test that future items remain in the input set until they are due."""
        future_timestamp = int(time.time()) + 3
        message_id = "future_message"

        # Add a future item and process immediately (should not be processed yet)
        self.add_message_to_set(message_id, future_timestamp)
        self.flow_dozer(False)  # Process one shot immediately

        remaining_due_items = self.flow_dozer.input_set.fetch_due_items(future_timestamp)
        self.assertEqual(len(remaining_due_items), 1, "Future message should remain in the input set")

        # Wait for the item to become due and process again
        time.sleep(3)
        self.flow_dozer(False)  # Process one shot immediately

        remaining_due_items = self.flow_dozer.input_set.fetch_due_items(future_timestamp)
        self.assertEqual(len(remaining_due_items), 0, "No due items should remain in the input set")

        # Verify notification message
        self.verify_notification_message(message_id)


if __name__ == "__main__":
    unittest.main()
