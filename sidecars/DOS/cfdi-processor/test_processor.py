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
            self.assertEqual(payload['UsoCFDI'], "S01")
            self.assertEqual(payload['MetodoPago'], "PPD")
            self.assertEqual(payload["FormaPago"], "99")
            self.assertEqual(payload['Moneda'], "MXN")
            self.assertEqual(payload['Serie'], "17317")
            self.assertEqual(payload['Comentarios'], "Esta factura es un quilombo")

            # Verify the 'Conceptos' array content
            conceptos = payload['Conceptos']
            self.assertEqual(len(conceptos), 1)  # Expecting one item

            # First item
            concepto1 = conceptos[0]
            self.assertEqual(concepto1['ClaveProdServ'], "78101800")
            self.assertEqual(concepto1['Cantidad'], "1")
            self.assertEqual(concepto1['ClaveUnidad'], "E48")
            self.assertEqual(concepto1['Unidad'], "Unidad de servicio")
            self.assertEqual(concepto1['ValorUnitario'], "2200.0")
            self.assertEqual(concepto1['Importe'], "2200.0")
            self.assertEqual(concepto1['Descripcion'], "SERVICIO DE FLETE NACAJUCA 1 A 5 REPARTOS NO. DE TRANSPORTE 289822 NO. DE RUTA 310753 SALIO DE CEDIS IXTACOMITAN UNIDAD 3.5 TONELADAS")

            # Verify 'Impuestos' -> 'Traslados' for the first item
            impuestos_trans = concepto1['Impuestos']['Traslados']
            self.assertEqual(len(impuestos_trans), 1)
            traslado1 = impuestos_trans[0]
            self.assertEqual(traslado1['Base'], "2200.0")
            self.assertEqual(traslado1['Impuesto'], "002")
            self.assertEqual(traslado1['TipoFactor'], "Tasa")
            self.assertEqual(traslado1['TasaOCuota'], "0.16")
            self.assertEqual(traslado1['Importe'], 352.0)

            impuestos_retens = concepto1['Impuestos']['Retenidos']
            self.assertEqual(len(impuestos_retens), 1)
            reten1 = impuestos_retens[0]
            self.assertEqual(reten1['Base'], "2200.0")
            self.assertEqual(reten1['Impuesto'], "002")
            self.assertEqual(reten1['TipoFactor'], "Tasa")
            self.assertEqual(reten1['TasaOCuota'], "0.04")
            self.assertEqual(reten1['Importe'], 88.0)

            node_cp = payload["CartaPorte"]
            self.assertEqual(node_cp["TranspInternac"], "Sí")
            self.assertEqual(node_cp["EntradaSalidaMerc"], "Salida")
            self.assertEqual(node_cp["PaisOrigenDestino"], "USA")
            self.assertEqual(node_cp["ViaEntradaSalida"], "03")
            self.assertEqual(node_cp["TotalDistRec"], 1000.0)

            # Verify the 'Ubicaciones -> Ubicacion' array content
            locations = node_cp["Ubicaciones"]["Ubicacion"]
            self.assertEqual(len(locations), 2)  # Expecting two item

            # First item
            location1 = locations[0]
            self.assertEqual(location1["NombreRemitenteDestinatario"], "THYSSENKRUPP PRESTA DE MEXICO, S.A. DE C.V. PM1")
            self.assertEqual(location1["TipoUbicacion"], "Origen")
            self.assertEqual(location1["RFCRemitenteDestinatario"], "TPM9809038X0")
            self.assertEqual(location1["FechaHoraSalidaLlegada"], "2023-10-05T07:00:00")
            self.assertEqual(location1["Domicilio"]["Estado"], "PUE")
            self.assertEqual(location1["Domicilio"]["Pais"], "MEX")
            self.assertEqual(location1["Domicilio"]["CodigoPostal"], "74160")

            # Second item
            location2 = locations[1]
            self.assertEqual(location2["NombreRemitenteDestinatario"], "Nexteer PLANTA 69")
            self.assertEqual(location2["TipoUbicacion"], "Destino")
            self.assertEqual(location2["FechaHoraSalidaLlegada"], "2023-10-05T13:50:00")
            self.assertEqual(location2["RFCRemitenteDestinatario"], "STE071214BE7")
            self.assertEqual(location2["DistanciaRecorrida"], 300)
            self.assertEqual(location2["Domicilio"]["Estado"], "QUE")
            self.assertEqual(location2["Domicilio"]["Pais"], "MEX")
            self.assertEqual(location2["Domicilio"]["CodigoPostal"], "76246")

            # Verify the 'FiguraTransporte -> TiposFigura' array content
            transporters = node_cp["FiguraTransporte"]['TiposFigura']
            self.assertEqual(len(transporters), 1)  # Expecting one item

            # First item
            transporter1 = transporters[0]
            self.assertEqual(transporter1["TipoFigura"], "01")
            self.assertEqual(transporter1["RFCFigura"], "CALJ741208LN5")
            self.assertEqual(transporter1["NumLicencia"], "PUE0011259")
            self.assertEqual(transporter1["NombreFigura"], "JUAN RENE CARRASCO LIZANA")

            # Verify the 'Mercancias -> Mercancia' array content
            goods = node_cp["Mercancias"]['Mercancia']
            self.assertEqual(len(goods), 1)  # Expecting one item

            # First item
            good1 = goods[0]
            self.assertEqual(good1["BienesTransp"], "25174200")
            self.assertEqual(good1["Descripcion"], "Sistema de dirección")
            self.assertEqual(good1["ClaveUnidad"], "H87")
            self.assertEqual(good1["Cantidad"], "4224")
            self.assertEqual(good1["PesoEnKg"], 723)
            self.assertEqual(good1["FraccionArancelaria"], "87089400")

            return ["5c06fa8b3bbe6"] # A counterfeit document id from PAC

        # Push a message to the input queue
        test_message = json.dumps({"fkeys": {"F-Api-Key": "123qwe", "F-Secret-Key": "1234qwer"}, "receipt": {
            "_id": "671d15d4224bb17cdb9d747a",
            "owner": 'J4NUSX',
            "receptor_rfc": 'PACE8001104V2',
            "receptor_data_ref": "6169fc02637e1",
            "items": [
                {
                    "fiscal_product_id": "78101800",
                    "product_quantity": 1,
                    "fiscal_product_unit": "E48",
                    "product_unit": "Unidad de servicio",
                    "product_unit_price": 2200.0,
                    "product_amount": 2200.0,
                    "product_desc": "SERVICIO DE FLETE NACAJUCA 1 A 5 REPARTOS NO. DE TRANSPORTE 289822 NO. DE RUTA 310753 SALIO DE CEDIS IXTACOMITAN UNIDAD 3.5 TONELADAS",
                    "product_transfers": [
                        {
                            "base": 2200.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.16,
                            "amount": 352.0
                        }
                    ],
                    "product_deductions": [
                        {
                            "base": 2200.0,
                            "fiscal_type": "002",
                            "fiscal_factor": "Tasa",
                            "rate": 0.04,
                            "amount": 88.0
                        }
                    ]
                }
            ],
            "serie": "17317",
            "purpose": "S01",
            "payment_way": "99",
            "payment_method": "PPD",
            "document_currency": "MXN",
            "exchange_rate": 1.0,
            "comments": "Esta factura es un quilombo",
            "bol": {
                "ver": "3.1",
                "is_international": True,
                "sum_dist_traveled": 1000.0,        # sum of the distances traveled (km)
                "is_step_out": True,
                "origin_destiny_country": "USA",    # ISO 3166-1 alpha-3 Code
                "in_out_via": "03",
                "transporters": [
                    {
                        "name": "JUAN RENE CARRASCO LIZANA",
                        "type": "01",
                        "rfc": "CALJ741208LN5",
                        "license": "PUE0011259"
                    }
                ],
                "locations": [
                    {
                        "name": "THYSSENKRUPP PRESTA DE MEXICO, S.A. DE C.V. PM1",
                        "rfc": "TPM9809038X0",
                        "type": "Origen",
                        "time": "2023-10-05T07:00:00",
                        "address": {
                            "state": "PUE",
                            "country": "MEX",
                            "zip": "74160",
                        }
                    },
                    {
                        "name": "Nexteer PLANTA 69",
                        "rfc": "STE071214BE7",
                        "type": "Destino",
                        "time": "2023-10-05T13:50:00",
                        "distance": 300,
                        "address": {
                            "state": "QUE",
                            "country": "MEX",
                            "zip": "76246"
                        }
                    }
                ],
                "merchandise": [
                    {
                        "sku": "25174200",
                        "desc": "Sistema de dirección",
                        "unit": "H87",
                        "quantity": 4224,
                        "kgs": 723,
                        "tariff_fraction": "87089400",
                    }
                ],
            },
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
            "comments": "Esta factura es un quilombo",
            "bol": {
                "ver": "3.1",
                "is_international": False,
            },
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
