import time
import redis
import traceback
import os
import sys
import requests
import json
import argparse
import logging
from abc import ABC, abstractmethod
from pythonjsonlogger import jsonlogger
from celery import Celery

redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = os.getenv("REDIS_PORT", "6379")

celery_app = Celery(
    "tasks",
    broker=f"redis://{redis_host}:{redis_port}/1",
    backend=f"redis://{redis_host}:{redis_port}/2")

# Set up JSON logging
logger = logging.getLogger()
log_handler = logging.StreamHandler(sys.stdout)
formatter = jsonlogger.JsonFormatter('%(asctime)s %(name)s %(levelname)s %(message)s')
log_handler.setFormatter(formatter)
logger.addHandler(log_handler)
logger.setLevel(logging.DEBUG if os.getenv("DEBUG", "False").lower() == "true" else logging.INFO)

ghost = os.getenv("PAC_HOST", "sandbox.factura.com/api")


def redis_connected(func):
    """Decorator to manage Redis connection for a function"""
    def wrapper(self, *args, **kwargs):
        client = connect_to_redis()
        try:
            return func(self, client, *args, **kwargs)
        except Exception as e:
            logger.exception("Redis connection error")
            raise e
        finally:
            client.close()
    return wrapper


def connect_to_redis():
    """Opens a connection to Redis"""
    redis_host = os.getenv("REDIS_HOST")
    redis_port = os.getenv("REDIS_PORT")

    if not redis_host or not redis_port:
        logger.error("Environment variables REDIS_HOST or REDIS_PORT not set")
        raise ValueError("Environment variables REDIS_HOST or REDIS_PORT not set")

    connection_str = f"redis://{redis_host}:{redis_port}/0"
    return redis.Redis.from_url(connection_str)


class QueueNotFoundException(Exception):
    """Custom exception for missing Redis queues."""
    pass


class RedisQueue:
    """Redis queue handler"""
    def __init__(self, queue_name):
        self.queue_name = queue_name

    @redis_connected
    def is_present(self, client):
        return client.exists(self.queue_name)

    @redis_connected
    def ping(self, client):
        """Check the connection to Redis."""
        client.ping()

    @redis_connected
    def push(self, client, message):
        """Push a message to the tail of the queue"""
        client.lpush(self.queue_name, message)

    @redis_connected
    def pop(self, client):
        """Pop a message from the head of the queue (blocking)"""
        m = client.brpop(self.queue_name)
        return m[1].decode("utf-8") if m else None


class RedisHashSet:
    """Redis hashset handler"""
    def __init__(self, hash_name, ttl=604800):
        self.hash_name = hash_name
        self.ttl = ttl

    @redis_connected
    def put(self, client, plain_text_key, plain_text_value):
        client.hset(self.hash_name, plain_text_key, plain_text_value)
        client.expire(self.hash_name, self.ttl)

    @redis_connected
    def get(self, client, plain_text_key):
        return client.hget(self.hash_name, plain_text_key)


class PacErrorResponse(Exception):
    """Custom exception for PAC's error response."""
    pass


class PacConnIssue(Exception):
    """Custom exception for PAC's unsuccessful connection."""
    pass

class AbstractStages(ABC):

    @abstractmethod
    def percolate_msg(self, original_msg):
        """Set aside raw msg from the original message"""

    @abstractmethod
    def render_payload(self, raw_msg):
        """Converts a raw message into a structured payload"""

    @abstractmethod
    def dispatch(self, third_party_keys, payload):
        """Dispatches the payload for further processing"""

    @abstractmethod
    def render_good_notification(self, *args):
        """Generates a notification for successful processing"""

    @abstractmethod
    def render_bad_notification(self, *args):
        """Generates a notification for failed processing"""


class AbstractProcessor(object):

    _OFFSET_RES_LIST_FIRST_ELEMENT = 1

    def __init__(self, stages_impt, input_queue_impt, notification_queue_impt, err_hset_impt, relay_handler=None):
        self.input_queue = input_queue_impt
        self.notification_queue = notification_queue_impt
        self.stages = stages_impt
        self.err_hset = err_hset_impt
        self.relay_handler = relay_handler

    def __call__(self, wait_for_queue, interval=-1):
        """Process messages in a loop"""
        if wait_for_queue:
            while not self.input_queue.is_present():
                logger.info(f"Input queue '{self.input_queue.queue_name}' not found. Waiting for it to become available...")
                time.sleep(10)
        else:
            if not self.input_queue.is_present():
                logger.error(f"Input queue '{self.input_queue.queue_name}' does not exist.")
                raise QueueNotFoundException(f"Input queue '{self.input_queue.queue_name}' does not exist yet.")

        while True:
            message = self.input_queue.pop()
            if message:
                self.handle_message(message)
            if interval < 0:
                break
            time.sleep(interval)

    def handle_message(self, message):
        """Handle processing of a message"""
        path_prefix, third_party_keys, raw_msg, raw_msg_correlation_id, raw_msg_owner, raw_msg_receptor_rfc = self.stages.percolate_msg(message)
        notif_args = [raw_msg_correlation_id]
        try:
            payload = self.stages.render_payload(raw_msg)
            res_list = self.stages.dispatch(third_party_keys, payload)
            if res_list and type(res_list) is list:
                notif_args += res_list
            notif_json = self.stages.render_good_notification(*notif_args)
            if self.relay_handler:
                self.relay_handler(path_prefix, raw_msg_correlation_id, notif_args[self._OFFSET_RES_LIST_FIRST_ELEMENT], raw_msg_receptor_rfc, raw_msg_owner, third_party_keys)
        except Exception as e:
            logger.error("Processing failed", exc_info=True)
            notif_json = self.stages.render_bad_notification(*notif_args)
            self.err_hset.put(raw_msg_correlation_id, self.render_exception(e))

        self.notification_queue.push(notif_json)

    @staticmethod
    def render_exception(exception):
        exception_data = {
            "type": type(exception).__name__,
            "message": str(exception)
        }
        return json.dumps(exception_data)


class InvoiceCreationStages(AbstractStages):

    def __init__(self, dispatcher=None):
        super().__init__()
        self.dispatcher = dispatcher or self.default_dispatcher

    def percolate_msg(self, original_msg):
        d = json.loads(original_msg)
        logger.info("Percolating message", extra={"msg_content": d})
        path_prefix = "{}/fiscal-engine/invoices/{}".format(d['receipt']['owner'], d['receipt']['receptor_rfc'])
        return path_prefix, d['fkeys'], d['receipt'], d['receipt']['_id'], d['receipt']['owner'], d['receipt']['receptor_rfc']

    def render_payload(self, receipt):
        """Construct the payload for the third-party API"""
        bol = receipt.get("bol")
        payload = {
            "Receptor": {"UID": receipt.get("receptor_data_ref")},
            "TipoDocumento": "factura",
            "Conceptos": [
                {
                    "ClaveProdServ": item.get("fiscal_product_id"),
                    "Cantidad": item.get("product_quantity"),
                    "ClaveUnidad": item.get("fiscal_product_unit"),
                    "Unidad": item.get("product_unit"),
                    "ValorUnitario": item.get("product_unit_price"),
                    "Descripcion": item.get("product_desc"),
                    "Impuestos": {
                        "Traslados": [
                            {
                                "Base": transfer.get("base"),
                                "Impuesto": transfer.get("fiscal_type"),
                                "TipoFactor": transfer.get("fiscal_factor"),
                                "TasaOCuota": str(transfer.get("rate")),
                                "Importe": transfer.get("amount")
                            }
                            for transfer in item.get("product_transfers", [])
                        ],
                        "Retenidos": [
                            {
                                "Base": retention.get("base"),
                                "Impuesto": retention.get("fiscal_type"),
                                "TipoFactor": retention.get("fiscal_factor"),
                                "TasaOCuota": str(retention.get("rate")),
                                "Importe": retention.get("amount")
                            }
                            for retention in item.get("product_deductions", [])
                        ]
                    }
                }
                for item in receipt.get("items", [])
            ],
            "UsoCFDI": receipt.get("purpose"),
            "Serie": receipt.get("serie"),
            "FormaPago": receipt.get("payment_way"),
            "MetodoPago": receipt.get("payment_method"),
            "Moneda": receipt.get("document_currency"),
            "TipoCambio": str(receipt.get("exchange_rate")),
            "Comentarios": receipt.get("comments"),
            "EnviarCorreo": False,
            "CartaPorte": {
                "Version": bol.get("ver"),
                "TranspInternac": "Si" if bol.get("is_international") else "No",
                "TotalDistRec": bol.get("sum_dist_traveled"),
                "FiguraTransporte" : {
                    "TiposFigura": [
                        {
                            "TipoFigura": operator.get("type"),
                            "RFCFigura": operator.get("rfc"),
                            "NumLicencia": operator.get("license"),
                            "NombreFigura": operator.get("name")
                        }
                        for operator in bol.get("transporters", [])
                    ]
                },
                "Ubicaciones" : {
                    "Ubicacion": []
                },
                "Mercancias": {
                    "Mercancia": []
                },
            },
        }

        node_cp = payload["CartaPorte"]

        # situational elements for
        if bol.get("is_international"):
            node_cp["EntradaSalidaMerc"] = "Salida" if bol.get("is_step_out") else "Entrada"
            node_cp["PaisOrigenDestino"] = bol.get("origin_destiny_country")
            node_cp["ViaEntradaSalida"] = bol.get("in_out_via")

        for location in bol.get("locations", []):
            item = {
                "NombreRemitenteDestinatario": location.get("name"),
                "RFCRemitenteDestinatario": location.get("rfc"),
                "TipoUbicacion": location.get("type"),
                "FechaHoraSalidaLlegada": location.get("time"),
                "Domicilio": {
                    "Estado": location["address"]["state"],
                    "Pais": location["address"]["country"],
                    "CodigoPostal": location["address"]["zip"],
                }
            }

            # Optional fields
            if 'distance' in location:
                item["DistanciaRecorrida"] = location["distance"]

            node_cp["Ubicaciones"]["Ubicacion"].append(item)

        for good in bol.get("merchandise", []):
            item = {
                "BienesTransp": good.get("sku"),
                "Descripcion": good.get("desc"),
                "ClaveUnidad": good.get("unit"),
                "PesoEnKg": good.get("kgs"),
                "Cantidad": good.get("quantity"),
            }

            node_cp["Mercancias"]["Mercancia"].append(item)

        return payload

    def dispatch(self, third_party_keys, payload):
        """
        Dispatch the payload using the provided dispatcher.
        Defaults to the real `Factura.com` dispatcher.
        """
        return self.dispatcher(third_party_keys, payload)

    @staticmethod
    def default_dispatcher(third_party_keys, payload):
        """
        Send the payload to the third-party API for CFDI creation on Factura.com.

        This function sends an HTTP POST request to the Factura.com API to create a CFDI document,
        using credentials and other details obtained from environment variables.
        """
        headers = {
            'Content-Type': 'application/json',
        }
        headers.update(third_party_keys)
        host = ghost
        endpoint = f"https://{host}/v4/cfdi40/create"

        try:
            response = requests.post(endpoint, headers=headers, data=json.dumps(payload))
            if response.status_code == 200:
                dictResponse = response.json()

                if dictResponse["response"] == "success":
                    logger.info("API response", extra={"response": response.text})
                    return [dictResponse["UUID"]]

                elif dictResponse["response"] == "error":
                    logger.error("API request failed (payload related)", exc_info=True)
                    raise PacErrorResponse(dictResponse['message'])
            else:
                logger.error("API request failed", exc_info=True)
                raise PacErrorResponse(f"Something went wrong. Status code {response.status_code}")

        except requests.exceptions.RequestException as e:
            logger.error("API request failed", exc_info=True)
            raise PacConnIssue(e)

    def render_good_notification(self, *args):
        if len(args) < 2:
            raise Exception("Missing required arguments: receipt_id and doc_uuid")

        receipt_id, doc_uuid = args[:2]  # Expecting at least 2 arguments
        d = {
            "type": "GOOD_OUTCOME_FROM_PROCESSOR",
            "timestamp": int(time.time()),
            "receipt_id": receipt_id,
            "doc_uuid": doc_uuid,
        }
        return json.dumps(d)

    def render_bad_notification(self, *args):
        if len(args) < 1:
            raise Exception("Missing required argument: receipt_id")

        receipt_id = args[0]  # Expecting at least 1 argument
        d = {
            "type": "BAD_OUTCOME_FROM_PROCESSOR",
            "timestamp": int(time.time()),
            "receipt_id": receipt_id,
        }
        return json.dumps(d)


class InvoiceCancelationStages(AbstractStages):

    def __init__(self, dispatcher=None):
        super().__init__()
        self.dispatcher = dispatcher or self.default_dispatcher

    def percolate_msg(self, original_msg):
        d = json.loads(original_msg)
        logger.info("Percolating message", extra={"msg_content": d})
        path_prefix = None
        owner = None
        receptor_rfc = None
        return path_prefix, d['fkeys'], d['invoice_cancelation'], d['invoice_cancelation']['receipt_id'], owner, receptor_rfc

    def render_payload(self, raw_msg):
        """Construct the payload for the third-party API"""
        meta = {
            "doc_uuid": raw_msg.get("doc_uuid"), # It shall be used and remove at dispatch stage
        }
        data = {
            "motivo": raw_msg.get("purpose"),
        }

        replacement = raw_msg.get("replacement", None)
        if replacement:
            data["folioSustituto"] = replacement

        return {'meta': meta, 'data': data}

    def dispatch(self, third_party_keys, payload):
        """
        Dispatch the payload using the provided dispatcher.
        Defaults to the real `Factura.com` dispatcher.
        """
        return self.dispatcher(third_party_keys, payload)

    @staticmethod
    def default_dispatcher(third_party_keys, payload):
        """
        Send the payload to the third-party API for CFDI creation on Factura.com.
        """
        headers = {
            'Content-Type': 'application/json',
        }
        headers.update(third_party_keys)
        host = ghost
        doc_uuid = payload['meta']['doc_uuid']
        del payload['meta'] # It is not a legit key, so It shall go

        endpoint = f"https://{host}/v4/cfdi40/{doc_uuid}/cancel"

        try:
            response = requests.post(endpoint, headers=headers, data=json.dumps(payload['data']))
            if response.status_code == 200:
                dictResponse = response.json()

                if dictResponse["response"] == "success":
                    logger.info("API response", extra={"response": response.text})
                    return None

                elif dictResponse["response"] == "error":
                    logger.error("API request failed (payload related)", exc_info=True)
                    raise PacErrorResponse(dictResponse['message'])
            else:
                logger.error("API request failed", exc_info=True)
                raise PacErrorResponse(f"Something went wrong. Status code {response.status_code}")

        except requests.exceptions.RequestException as e:
            logger.error("API request failed", exc_info=True)
            raise PacConnIssue(e)

    def render_good_notification(self, *args):
        if len(args) < 1:
            raise Exception("Missing required argument: receipt_id")

        receipt_id = args[0]  # Expecting at least 1 argument
        d = {
            "type": "GOOD_OUTCOME_FROM_INVOICE_CANCELATION",
            "timestamp": int(time.time()),
            "receipt_id": receipt_id,
        }
        return json.dumps(d)

    def render_bad_notification(self, *args):
        if len(args) < 1:
            raise Exception("Missing required argument: receipt_id")

        receipt_id = args[0]  # Expecting at least 1 argument
        d = {
            "type": "BAD_OUTCOME_FROM_INVOICE_CANCELATION",
            "timestamp": int(time.time()),
            "receipt_id": receipt_id,
        }
        return json.dumps(d)

class InvoiceCreationProcessor(AbstractProcessor):
    """Processor for handling Redis queues"""
    def __init__(self, input_queue_name, notification_queue_name, err_hset_name):
        super().__init__(InvoiceCreationStages(), RedisQueue(input_queue_name), RedisQueue(notification_queue_name), RedisHashSet(err_hset_name), self.copy_to_bucket)

    @staticmethod
    def copy_to_bucket(path_prefix, receipt_id, uuid, receptor_rfc, owner, third_party_keys):
        try:
            _DOMAIN_EXT = ('xml', 'pdf')
            dist_job_handler = lambda result_type: celery_app.send_task('uploadToBucketTask', (path_prefix, uuid, result_type, ghost, third_party_keys))
            results = list(map(dist_job_handler, _DOMAIN_EXT))
            for idx, r in enumerate(results):
                logger.info(f"{_DOMAIN_EXT[idx].capitalize()} upload result: {r.get(timeout=10)}")
        except Exception as e:
            # This exception shall be notified as an alert later
            logger.error(f"{_DOMAIN_EXT[idx].capitalize()} upload failed: {e}")
            return

        try:
            celery_app.send_task('spamTheWorld', (owner, receptor_rfc, uuid, receipt_id))  # Non-blocking call
        except Exception as e:
            # This exception shall be notified as an alert later
            logger.error(f"Failed to spam the world: {e}")

class InvoiceCancelationProcessor(AbstractProcessor):
    """Processor for handling Redis queues"""
    def __init__(self, input_queue_name, notification_queue_name, err_hset_name):
        super().__init__(InvoiceCancelationStages(), RedisQueue(input_queue_name), RedisQueue(notification_queue_name), RedisHashSet(err_hset_name))


if __name__ == "__main__":

    def _set_cmdargs_up():
        """parses the cmd line arguments at the call"""
        psr_desc="cfdi-omni-processor's command line control interface"
        psr_epi="The command line interface determines processor's flavor"

        psr = argparse.ArgumentParser(description=psr_desc, epilog=psr_epi)
        psr.add_argument(
            '-p', '--processor',
            dest='processor_flavor', help='specify the flavor to use'
        )
        return psr.parse_args()

    def spinup_invoice_creation():
        input_queue_name = os.getenv("PROCESSOR_INPUT_QUEUE_NAME", "processorInput")
        notification_queue_name = os.getenv("NOTIFICATIONS_QUEUE_NAME", "notifications")
        err_hset_name = os.getenv("ERR_HSET_NAME", "processorErrs")
        poll_interval = int(os.getenv("POLL_INTERVAL", 1))
        wait_for_queue = os.getenv("WAIT_FOR_QUEUE", "false").lower() == "true"
        processor = InvoiceCreationProcessor(input_queue_name, notification_queue_name, err_hset_name)
        processor(wait_for_queue, poll_interval)

    def spinup_invoice_cancelation():
        input_queue_name = os.getenv("PROCESSOR_CANCEL_INPUT_QUEUE_NAME", "processorCancelInput")
        notification_queue_name = os.getenv("NOTIFICATIONS_QUEUE_NAME", "notifications")
        err_hset_name = os.getenv("ERR_HSET_NAME", "processorCancelErrs")
        poll_interval = int(os.getenv("POLL_INTERVAL", 1))
        wait_for_queue = os.getenv("WAIT_FOR_QUEUE", "false").lower() == "true"
        processor = InvoiceCancelationProcessor(input_queue_name, notification_queue_name, err_hset_name)
        processor(wait_for_queue, poll_interval)

    args = _set_cmdargs_up()

    if args.processor_flavor is None:
        logger.error("No processor flavor specified. Use --processor to specify one.")
        sys.exit(1)

    try:
        switcher = {
            'invoice_creation': spinup_invoice_creation,
            'invoice_cancelation': spinup_invoice_cancelation
        }
        switcher[args.processor_flavor]()
    except KeyboardInterrupt:
        logger.info("Exiting")
    except Exception as e:
        logger.critical("Critical error in processor", exc_info=True)
        sys.exit(1)
