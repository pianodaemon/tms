import os
import json
import unittest
from unittest.mock import patch
from testcontainers.redis import RedisContainer
from processor import InvoiceCreationProcessor, InvoiceCancelationProcessor, RedisQueue, QueueNotFoundException


class TestRedisQueue(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Start a Redis container
        cls.redis_container = RedisContainer("redis:latest")
        cls.redis_container.start()

        # Set environment variables for Redis connection
        os.environ["REDIS_HOST"] = cls.redis_container.get_container_host_ip()
        os.environ["REDIS_PORT"] = str(cls.redis_container.get_exposed_port(6379))

    @classmethod
    def tearDownClass(cls):
        # Stop the Redis container
        cls.redis_container.stop()

    def setUp(self):
        self.queue_name = 'test_queue'
        self.redis_queue = RedisQueue(self.queue_name)

    def test_is_present(self):
        self.redis_queue.push("test_message")
        result = self.redis_queue.is_present()
        self.assertTrue(result)

    def test_push_and_pop(self):
        self.redis_queue.push("test_message")
        message = self.redis_queue.pop()
        self.assertEqual(message, "test_message")


class TestInvoiceCreationProcessor(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Start a Redis container
        cls.redis_container = RedisContainer("redis:latest")
        cls.redis_container.start()

        # Set environment variables for Redis connection
        os.environ["REDIS_HOST"] = cls.redis_container.get_container_host_ip()
        os.environ["REDIS_PORT"] = str(cls.redis_container.get_exposed_port(6379))

    @classmethod
    def tearDownClass(cls):
        # Stop the Redis container
        cls.redis_container.stop()

    @staticmethod
    def copy_to_nowhere(path_prefix, receipt_id, uuid, receptor_rfc, owner, third_party_keys):
        pass

    def setUp(self):
        self.processor = InvoiceCreationProcessor("test_queue", "notification_queue", "processor_errs")
        self.processor.relay_handler = self.copy_to_nowhere

    def test_queue_not_found_exception(self):
        with self.assertRaises(QueueNotFoundException):
            self.processor.__call__(0)

    def test_process_message(self):
        # Mock the dispatcher
        def mock_dispatcher(third_party_keys, payload):

            self.assertEqual(third_party_keys["F-Api-Key"], "123qwe")
            self.assertEqual(third_party_keys["F-Secret-Key"], "1234qwer")

            self.assertEqual(payload['Receptor']['UID'], "6169fc02637e1")
            self.assertEqual(payload['UsoCFDI'], "P01")
            self.assertEqual(payload['MetodoPago'], "PUE")
            self.assertEqual(payload['Moneda'], "MXN")
            self.assertEqual(payload['Serie'], "17317")
            self.assertEqual(payload['Comentarios'], "Esta factura es un quilombo")

            # Verify the 'Conceptos' array content
            conceptos = payload['Conceptos']
            self.assertEqual(len(conceptos), 2)  # Expecting two items

            # First item
            concepto1 = conceptos[0]
            self.assertEqual(concepto1['ClaveProdServ'], "10101504")
            self.assertEqual(concepto1['Cantidad'], 1)
            self.assertEqual(concepto1['ClaveUnidad'], "H87")
            self.assertEqual(concepto1['Unidad'], "Unidad")
            self.assertEqual(concepto1['ValorUnitario'], 100.0)
            self.assertEqual(concepto1['Descripcion'], "Consulting Services")

            # Verify 'Impuestos' -> 'Traslados' for the first item
            impuestos1 = concepto1['Impuestos']['Traslados']
            self.assertEqual(len(impuestos1), 1)
            traslado1 = impuestos1[0]
            self.assertEqual(traslado1['Base'], 100.0)
            self.assertEqual(traslado1['Impuesto'], "002")
            self.assertEqual(traslado1['TipoFactor'], "Tasa")
            self.assertEqual(traslado1['TasaOCuota'], "0.16")
            self.assertEqual(traslado1['Importe'], 16.0)

            # Second item
            concepto2 = conceptos[1]
            self.assertEqual(concepto2['ClaveProdServ'], "20101010")
            self.assertEqual(concepto2['Cantidad'], 2)
            self.assertEqual(concepto2['ClaveUnidad'], "EA")
            self.assertEqual(concepto2['Unidad'], "Piece")
            self.assertEqual(concepto2['ValorUnitario'], 200.0)
            self.assertEqual(concepto2['Descripcion'], "Development Services")

            # Verify 'Impuestos' -> 'Traslados' for the second item
            impuestos2 = concepto2['Impuestos']['Traslados']
            self.assertEqual(len(impuestos2), 1)
            traslado2 = impuestos2[0]
            self.assertEqual(traslado2['Base'], 200.0)
            self.assertEqual(traslado2['Impuesto'], "002")
            self.assertEqual(traslado2['TipoFactor'], "Tasa")
            self.assertEqual(traslado2['TasaOCuota'], "0.16")
            self.assertEqual(traslado2['Importe'], 32.0)
            return ["5c06fa8b3bbe6"] # A counterfeit document id from PAC

        # Push a message to the input queue
        test_message = json.dumps({"fkeys": {"F-Api-Key": "123qwe", "F-Secret-Key": "1234qwer"}, "receipt": {
            "_id": "671d15d4224bb17cdb9d747a",
            "owner": 'J4NUSX',
            "receptor_rfc": 'PACE8001104V2',
            "receptor_data_ref": "6169fc02637e1",
            "items": [
                {
                    "fiscal_product_id": "10101504",
                    "product_quantity": 1,
                    "fiscal_product_unit": "H87",
                    "product_unit": "Unidad",
                    "product_unit_price": 100.0,
                    "product_desc": "Consulting Services",
                    "product_transfers": [
                        {
                            "base": 100.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.16,
                            "amount": 16.0
                        }
                    ]
                },
                {
                    "fiscal_product_id": "20101010",
                    "product_quantity": 2,
                    "fiscal_product_unit": "EA",
                    "product_unit": "Piece",
                    "product_unit_price": 200.0,
                    "product_desc": "Development Services",
                    "product_transfers": [
                        {
                            "base": 200.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.16,
                            "amount": 32.0
                        }
                    ]
                }
            ],
            "serie": "17317",
            "purpose": "P01",
            "payment_way": "03",
            "payment_method": "PUE",
            "document_currency": "MXN",
            "exchange_rate": 1.0,
            "comments": "Esta factura es un quilombo"
        }})

        self.processor.input_queue.push(test_message)

        # Patch the dispatch method of the embedded FacturaStages object
        with patch.object(self.processor.stages, 'dispatch', side_effect=mock_dispatcher):
            # Process the message
            self.processor.__call__(False)

        notif_msg_generated = self.processor.notification_queue.pop()
        notif_dict_generated = json.loads(notif_msg_generated)
        self.assertEqual(notif_dict_generated['type'], "GOOD_OUTCOME_FROM_PROCESSOR")
        self.assertTrue(notif_dict_generated['receipt_id'], "Expecting '6169fc02637e1'")
        self.assertEqual(notif_dict_generated['doc_uuid'], "5c06fa8b3bbe6", "Expecting '5c06fa8b3bbe6'")
        self.assertTrue(isinstance(notif_dict_generated['timestamp'], int), "Time stamp value is not an integer")

    def test_err_storage(self):
        # Mock the dispatcher
        def mock_dispatcher(third_party_keys, payload):
            self.assertEqual(third_party_keys["F-Api-Key"], "123qwe")
            self.assertEqual(third_party_keys["F-Secret-Key"], "1234qwer")
            raise Exception("woooopssssssss!!")

        # Push a message to the input queue
        test_message = json.dumps({"fkeys": {"F-Api-Key": "123qwe", "F-Secret-Key": "1234qwer"}, "receipt": {
            "_id": "671d15d4224bb17cdb9d747a",
            "owner": 'J4NUSX',
            "receptor_rfc": 'PACE8001104V2',
            "receptor_data_ref": "6169fc02637e1",
            "items": [
                {
                    "fiscal_product_id": "10101504",
                    "product_quantity": 1,
                    "fiscal_product_unit": "H87",
                    "product_unit": "Unidad",
                    "product_unit_price": 100.0,
                    "product_desc": "Consulting Services",
                    "product_transfers": [
                        {
                            "base": 100.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.16,
                            "amount": 16.0
                        }
                    ]
                },
                {
                    "fiscal_product_id": "20101010",
                    "product_quantity": 2,
                    "fiscal_product_unit": "EA",
                    "product_unit": "Piece",
                    "product_unit_price": 200.0,
                    "product_desc": "Development Services",
                    "product_transfers": [
                        {
                            "base": 200.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.16,
                            "amount": 32.0
                        }
                    ]
                }
            ],
            "serie": "17317",
            "purpose": "P01",
            "payment_way": "03",
            "payment_method": "PUE",
            "document_currency": "MXN",
            "exchange_rate": 1.0,
            "comments": "Esta factura es un quilombo"
        }})

        self.processor.input_queue.push(test_message)

        # Patch the dispatch method of the embedded FacturaStages object
        with patch.object(self.processor.stages, 'dispatch', side_effect=mock_dispatcher):
            # Process the message
            self.processor.__call__(False)

        err_plain_text = self.processor.err_hset.get("671d15d4224bb17cdb9d747a")
        err_dict = json.loads(err_plain_text)
        self.assertEqual(err_dict['type'], "Exception")
        self.assertEqual(err_dict['message'], "woooopssssssss!!")

        notif_msg_generated = self.processor.notification_queue.pop()
        notif_dict_generated = json.loads(notif_msg_generated)
        self.assertEqual(notif_dict_generated['type'], "BAD_OUTCOME_FROM_PROCESSOR")
        self.assertTrue(notif_dict_generated['receipt_id'], "Expecting '6169fc02637e1'")
        self.assertTrue(isinstance(notif_dict_generated['timestamp'], int), "Time stamp value is not an integer")


class TestInvoiceCancelationProcessor(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        # Start a Redis container
        cls.redis_container = RedisContainer("redis:latest")
        cls.redis_container.start()

        # Set environment variables for Redis connection
        os.environ["REDIS_HOST"] = cls.redis_container.get_container_host_ip()
        os.environ["REDIS_PORT"] = str(cls.redis_container.get_exposed_port(6379))

    @classmethod
    def tearDownClass(cls):
        # Stop the Redis container
        cls.redis_container.stop()

    def setUp(self):
        self.processor = InvoiceCancelationProcessor("test_queue_xx", "notification_queue_xx", "processor_errs_xx")

    def test_queue_not_found_exception(self):
        with self.assertRaises(QueueNotFoundException):
            self.processor.__call__(0)

    def test_err_storage(self):
        # Mock the dispatcher
        def mock_dispatcher(third_party_keys, payload):
            self.assertEqual(third_party_keys["F-Api-Key"], "123qwe")
            self.assertEqual(third_party_keys["F-Secret-Key"], "1234qwer")
            raise Exception("what a terrible situation")

        # Push a message to the input queue
        test_message = json.dumps({"fkeys": {"F-Api-Key": "123qwe", "F-Secret-Key": "1234qwer"}, "invoice_cancelation": {
            "receipt_id": "671d15d4224bb17cdb9d747a",
            "doc_uuid": "5c06fa8b3bbe4",
            "purpose": "01",
            "replacement": "5c06fa8b3bbe5"
        }})

        self.processor.input_queue.push(test_message)

        # Patch the dispatch method of the embedded FacturaStages object
        with patch.object(self.processor.stages, 'dispatch', side_effect=mock_dispatcher):
            # Process the message
            self.processor.__call__(False)

        err_plain_text = self.processor.err_hset.get("671d15d4224bb17cdb9d747a")
        err_dict = json.loads(err_plain_text)
        self.assertEqual(err_dict['type'], "Exception")
        self.assertEqual(err_dict['message'], "what a terrible situation")

        notif_msg_generated = self.processor.notification_queue.pop()
        notif_dict_generated = json.loads(notif_msg_generated)
        self.assertEqual(notif_dict_generated['type'], "BAD_OUTCOME_FROM_INVOICE_CANCELATION")
        self.assertEqual(notif_dict_generated['receipt_id'], "671d15d4224bb17cdb9d747a")
        self.assertTrue(isinstance(notif_dict_generated['timestamp'], int), "Time stamp value is not an integer")

    def test_process_message(self):
        # Mock the dispatcher
        def mock_dispatcher(third_party_keys, payload):
            self.assertEqual(third_party_keys["F-Api-Key"], "123qwe")
            self.assertEqual(third_party_keys["F-Secret-Key"], "1234qwer")
            p = payload['data']
            self.assertEqual(p['folioSustituto'], "5c06fa8b3bbe5")
            self.assertEqual(p['motivo'], "01")

        # Push a message to the input queue
        test_message = json.dumps({"fkeys": {"F-Api-Key": "123qwe", "F-Secret-Key": "1234qwer"}, "invoice_cancelation": {
            "receipt_id": "671d15d4224bb17cdb9d747a",
            "doc_uuid": "5c06fa8b3bbe4",
            "purpose": "01",
            "replacement": "5c06fa8b3bbe5"
        }})

        self.processor.input_queue.push(test_message)

        # Patch the dispatch method of the embedded FacturaStages object
        with patch.object(self.processor.stages, 'dispatch', side_effect=mock_dispatcher):
            # Process the message
            self.processor.__call__(False)

        notif_msg_generated = self.processor.notification_queue.pop()
        notif_dict_generated = json.loads(notif_msg_generated)
        self.assertEqual(notif_dict_generated['type'], "GOOD_OUTCOME_FROM_INVOICE_CANCELATION")
        self.assertEqual(notif_dict_generated['receipt_id'], "671d15d4224bb17cdb9d747a")
        self.assertTrue(isinstance(notif_dict_generated['timestamp'], int), "Time stamp value is not an integer")


if __name__ == "__main__":
    unittest.main()
