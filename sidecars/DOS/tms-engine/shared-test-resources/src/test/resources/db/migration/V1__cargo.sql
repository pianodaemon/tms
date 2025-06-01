SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET search_path TO public;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TABLE boxes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    box_type VARCHAR(50) NOT NULL,          -- Store as a string (enum values)
    box_brand VARCHAR(50) NOT NULL,         -- Store as a string (enum values)
    number_serial VARCHAR(20) NOT NULL,
    number_plate VARCHAR(10) NOT NULL,
    number_plate_expiration date NOT NULL,
    number_axis SMALLINT NOT NULL,
    box_year INT NOT NULL,
    lease BOOLEAN DEFAULT false NOT NULL,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL
);

COMMENT ON COLUMN boxes.id IS 'Identificador unico de caja';
COMMENT ON COLUMN boxes.tenant_id IS 'Pertenece a determinado subscriptor';
COMMENT ON COLUMN boxes.name IS 'Nombre de la caja';
COMMENT ON COLUMN boxes.box_type IS 'Tipo de caja';
COMMENT ON COLUMN boxes.box_brand IS 'La marca de la caja'; 
COMMENT ON COLUMN boxes.number_plate IS 'Las placas de la caja';
COMMENT ON COLUMN boxes.number_plate_expiration IS 'Fecha de expiracion de placas';
COMMENT ON COLUMN boxes.number_axis IS 'Numero de ejes';
COMMENT ON COLUMN boxes.box_year IS 'Año de la caja';
COMMENT ON COLUMN boxes.lease IS 'Indica si la caja es de alquiler o propia';
COMMENT ON COLUMN boxes.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro de la caja';
COMMENT ON COLUMN boxes.creation_time IS 'El momento en que fue creada la caja';
COMMENT ON COLUMN boxes.blocked IS 'Indica si la caja fue logicamente eliminada';


CREATE TABLE customers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL
);

COMMENT ON COLUMN customers.id IS 'Identificador unico de cliente';
COMMENT ON COLUMN customers.tenant_id IS 'Pertenece a subscriptor';
COMMENT ON COLUMN customers.name IS 'Nombre con el que se le conoce a este cliente';
COMMENT ON COLUMN customers.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro de el cliente';
COMMENT ON COLUMN customers.creation_time IS 'El momento en que fue creado el cliente';
COMMENT ON COLUMN customers.blocked IS 'Indica si el cliente fue logicamente eliminado';


CREATE TABLE drivers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    first_surname VARCHAR(128) NOT NULL,
    second_surname VARCHAR(128) NOT NULL,
    license_number VARCHAR(128) NOT NULL,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT unique_license_per_tenant UNIQUE (tenant_id, license_number)
);

COMMENT ON COLUMN drivers.id IS 'Identificador unico de chofer';
COMMENT ON COLUMN drivers.tenant_id IS 'Pertenece a subscriptor';
COMMENT ON COLUMN drivers.name IS 'Nombre de pila';
COMMENT ON COLUMN drivers.first_surname IS 'Apellido paterno';
COMMENT ON COLUMN drivers.second_surname IS 'Apellido materno';
COMMENT ON COLUMN drivers.license_number IS 'Licencia de manejo';
COMMENT ON COLUMN drivers.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro del chofer';
COMMENT ON COLUMN drivers.creation_time IS 'El momento en que fue creado el chofer';
COMMENT ON COLUMN drivers.blocked IS 'Indica si el chofer fue logicamente eliminado';


CREATE TABLE vehicles (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    number_plate VARCHAR(10) NOT NULL,
    number_serial VARCHAR(128) NOT NULL,
    number_plate_expiration date NOT NULL,
    insurance_expiration date NOT NULL,
    number_axis SMALLINT NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,  -- Store as a string (enum values)
    vehicle_color VARCHAR(50) NOT NULL, -- Store as a string (enum values)
    vehicle_year INT NOT NULL,
    federal_conf VARCHAR(6) NOT NULL,  -- configuración autotransporte federal ( Carta porte )
    perf_dist_unit VARCHAR(50),         -- Store as a string (enum values)
    perf_vol_unit VARCHAR(50),          -- Store as a string (enum values)
    perf_scalar NUMERIC(10, 2),
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT vehicle_unique_number_plate UNIQUE (tenant_id, number_plate)
);

COMMENT ON COLUMN vehicles.id IS 'Identificador unico de el vehiculo';
COMMENT ON COLUMN vehicles.tenant_id IS 'Pertenece a subscriptor';
COMMENT ON COLUMN vehicles.number_plate IS 'Las placas de el vehiculo';
COMMENT ON COLUMN vehicles.number_plate_expiration IS 'Fecha de expiracion de las placas';
COMMENT ON COLUMN vehicles.insurance_expiration IS 'Fecha de expiracion del seguro';
    
COMMENT ON COLUMN vehicles.number_axis IS 'Numero de ejes';

COMMENT ON COLUMN vehicles.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro de el vehiculor';
COMMENT ON COLUMN vehicles.creation_time IS 'El momento en que fue creado el vehiculo';
COMMENT ON COLUMN vehicles.blocked IS 'Indica si el vehiculo fue logicamente eliminado';


CREATE TABLE agreements (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    customer_id UUID REFERENCES customers(id),
    receiver VARCHAR(128) NOT NULL,
    latitude_origin DOUBLE PRECISION NOT NULL,
    longitude_origin DOUBLE PRECISION NOT NULL,
    latitude_destiny DOUBLE PRECISION NOT NULL,
    longitude_destiny DOUBLE PRECISION NOT NULL,
    dist_unit VARCHAR(5) NOT NULL,
    dist_scalar NUMERIC(15, 6) NOT NULL,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT unique_route_per_customer UNIQUE (
        customer_id, latitude_origin, longitude_origin, latitude_destiny, longitude_destiny
    )
);

COMMENT ON COLUMN agreements.id IS 'Identificador unico de el acuerdo';
COMMENT ON COLUMN agreements.tenant_id IS 'Pertenece a subscriptor';

COMMENT ON COLUMN agreements.latitude_origin IS 'Distancia angular entre el origen y el ecuador';
COMMENT ON COLUMN agreements.longitude_origin IS 'Distancia angular entre el origen y el meridiano de Greenwich';
COMMENT ON COLUMN agreements.latitude_destiny IS 'Distancia angular entre el destino y el ecuador';
COMMENT ON COLUMN agreements.longitude_destiny IS 'Distancia angular entre el destino y el meridiano de Greenwich';

COMMENT ON COLUMN agreements.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro de el acuerdo';
COMMENT ON COLUMN agreements.creation_time IS 'El momento en que fue creado el acuerdo';
COMMENT ON COLUMN agreements.blocked IS 'Indica si el acuerdo fue logicamente eliminado';


CREATE TABLE patios (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    latitude_location DOUBLE PRECISION NOT NULL,
    longitude_location DOUBLE PRECISION NOT NULL,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    blocked boolean DEFAULT false NOT NULL
);

COMMENT ON COLUMN patios.id IS 'Identificador unico de el patio';
COMMENT ON COLUMN patios.tenant_id IS 'Pertenece a subscriptor';
COMMENT ON COLUMN patios.name IS 'Nombre del patio';
COMMENT ON COLUMN patios.latitude_location IS 'Distancia angular entre el patio y el ecuador';
COMMENT ON COLUMN patios.longitude_location IS 'Distancia angular entre el patio y el meridiano de Greenwich';
COMMENT ON COLUMN patios.last_touch_time IS 'Ultimo momento de actualizacion a cualquier parametro de el patio';
COMMENT ON COLUMN patios.creation_time IS 'El momento en que fue creado el patio';
COMMENT ON COLUMN patios.blocked IS 'Indica si el patio fue logicamente eliminado';


CREATE TABLE cargo_assignments (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id VARCHAR(255) NOT NULL,
    driver_id UUID REFERENCES drivers(id),
    vehicle_id UUID REFERENCES vehicles(id),
    latitude_location DOUBLE PRECISION,
    longitude_location DOUBLE PRECISION,
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL, 
    blocked boolean DEFAULT false NOT NULL
);


CREATE TABLE trans_log_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    dist_unit VARCHAR(5) NOT NULL,
    dist_scalar NUMERIC(15, 6) NOT NULL,
    fuel_consumption NUMERIC(10, 2),
    last_touch_time timestamp with time zone NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    cargo_assignment_id UUID REFERENCES cargo_assignments(id)
);


CREATE OR REPLACE FUNCTION alter_vehicle(
    _vehicle_id        UUID,
    _tenant_id         UUID,
    _number_plate      VARCHAR,
    _number_plate_expiration DATE,
    _number_serial     VARCHAR,
    _number_axis       SMALLINT,
    _insurance_expiration DATE,
    _vehicle_type      VARCHAR,
    _vehicle_color     VARCHAR,
    _vehicle_year      INT,
    _federal_conf      VARCHAR,
    _perf_dist_unit    VARCHAR,
    _perf_vol_unit     VARCHAR,
    _perf_scalar       NUMERIC
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit vehicle                                          >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        02/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _vehicle_id IS NULL THEN

            INSERT INTO vehicles (
                id,
                tenant_id,
                number_plate,
                number_plate_expiration,
                insurance_expiration,
                number_serial,
                number_axis,
                vehicle_type,
                vehicle_color,
                vehicle_year,
                federal_conf,
                perf_dist_unit,
                perf_vol_unit,
                perf_scalar,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _number_plate,
                _number_plate_expiration,
                _insurance_expiration,
                _number_serial,
                _number_axis,
                _vehicle_type,
                _vehicle_color,
                _vehicle_year,
                _federal_conf,
                _perf_dist_unit,
                _perf_vol_unit,
                _perf_scalar,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _vehicle_id;

        WHEN _vehicle_id IS NOT NULL THEN

            UPDATE vehicles
            SET
                tenant_id      = _tenant_id,
                number_plate   = _number_plate,
                number_plate_expiration = _number_plate_expiration,
                insurance_expiration = _insurance_expiration,
                number_serial  = _number_serial,
                number_axis    = _number_axis,
                vehicle_type   = _vehicle_type,
                vehicle_color  = _vehicle_color,
                vehicle_year   = _vehicle_year,
                federal_conf   = _federal_conf,
                perf_dist_unit = _perf_dist_unit,
                perf_vol_unit  = _perf_vol_unit,
                perf_scalar    = _perf_scalar,
                last_touch_time = current_moment
            WHERE id = _vehicle_id;

        ELSE
            RAISE EXCEPTION 'Invalid vehicle identifier: %', _vehicle_id;
    END CASE;

    RETURN (_vehicle_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);

END;
$$;


CREATE OR REPLACE FUNCTION alter_driver(
    _driver_id       UUID,
    _tenant_id       UUID,
    _name            VARCHAR,
    _first_surname   VARCHAR,
    _second_surname  VARCHAR,
    _license_number  VARCHAR
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit driver                                           >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        03/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _driver_id IS NULL THEN

            INSERT INTO drivers (
                id,
                tenant_id,
                name,
                first_surname,
                second_surname,
                license_number,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                _first_surname,
                _second_surname,
                _license_number,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _driver_id;

        WHEN _driver_id IS NOT NULL THEN

            UPDATE drivers
            SET
                tenant_id      = _tenant_id,
                name           = _name,
                first_surname  = _first_surname,
                second_surname = _second_surname,
                license_number = _license_number,
                last_touch_time = current_moment
            WHERE id = _driver_id;

        ELSE
            RAISE EXCEPTION 'Invalid driver identifier: %', _driver_id;
    END CASE;

    RETURN (_driver_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_patio(
    _patio_id     UUID,
    _tenant_id    UUID,
    _name         VARCHAR,
    _latitude     DOUBLE PRECISION,
    _longitude    DOUBLE PRECISION
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit patio                                            >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        03/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _patio_id IS NULL THEN

            INSERT INTO patios (
                id,
                tenant_id,
                name,
                latitude_location,
                longitude_location,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                _latitude,
                _longitude,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _patio_id;

        WHEN _patio_id IS NOT NULL THEN

            UPDATE patios
            SET
                tenant_id         = _tenant_id,
                name              = _name,
                latitude_location = _latitude,
                longitude_location = _longitude,
                last_touch_time = current_moment
            WHERE id = _patio_id;

        ELSE
            RAISE EXCEPTION 'Invalid patio identifier: %', _patio_id;
    END CASE;

    RETURN (_patio_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_box(
    _box_id UUID,
    _tenant_id   UUID,
    _name        VARCHAR,
    _box_type    VARCHAR,
    _number_axis   SMALLINT,
    _box_brand   VARCHAR,
    _number_serial VARCHAR,
    _number_plate  VARCHAR,
    _number_plate_expiration date,
    _box_year      INT,
    _lease         BOOLEAN
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit box                                              >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        28/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _box_id IS NULL THEN

            INSERT INTO boxes (
                id,
                tenant_id,
                name,
                box_type,
                box_brand,
                number_serial,
                number_plate,
                number_plate_expiration,
                number_axis,
                box_year,
                lease,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                _box_type,
                _box_brand,
                _number_serial,
                _number_plate,
                _number_plate_expiration,
                _number_axis,
                _box_year,
                _lease,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _box_id;

        WHEN _box_id IS NOT NULL THEN

            UPDATE boxes
            SET
                tenant_id = _tenant_id,
                last_touch_time = current_moment,
                name      = _name,
                box_type = _box_type,
                box_brand = _box_brand,
                number_serial = _number_serial,
                number_plate = _number_plate,
                number_axis = _number_axis,
                box_year = _box_year,
                lease = _lease,
                number_plate_expiration = _number_plate_expiration
            WHERE id = _box_id;

        ELSE
            RAISE EXCEPTION 'Invalid box identifier: %', _box_id;
    END CASE;

    RETURN (_box_id::UUID, ''::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_customer(
    _customer_id UUID,
    _tenant_id   UUID,
    _name        VARCHAR
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit customer                                         >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        03/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _customer_id IS NULL THEN

            INSERT INTO customers (
                id,
                tenant_id,
                name,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _customer_id;

        WHEN _customer_id IS NOT NULL THEN

            UPDATE customers
            SET
                tenant_id = _tenant_id,
                last_touch_time = current_moment,
                name      = _name
            WHERE id = _customer_id;

        ELSE
            RAISE EXCEPTION 'Invalid customer identifier: %', _customer_id;
    END CASE;

    RETURN (_customer_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_agreement(
    _agreement_id      UUID,
    _tenant_id         UUID,
    _customer_id       UUID,
    _receiver          VARCHAR,
    _latitude_origin   DOUBLE PRECISION,
    _longitude_origin  DOUBLE PRECISION,
    _latitude_destiny  DOUBLE PRECISION,
    _longitude_destiny DOUBLE PRECISION,
    _dist_unit         VARCHAR,
    _dist_scalar       NUMERIC
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit agreement                                        >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        03/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _agreement_id IS NULL THEN

            INSERT INTO agreements (
                id,
                tenant_id,
                customer_id,
                receiver,
                latitude_origin,
                longitude_origin,
                latitude_destiny,
                longitude_destiny,
                dist_unit,
                dist_scalar,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _customer_id,
                _receiver,
                _latitude_origin,
                _longitude_origin,
                _latitude_destiny,
                _longitude_destiny,
                _dist_unit,
                _dist_scalar,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _agreement_id;

        WHEN _agreement_id IS NOT NULL THEN

            UPDATE agreements
            SET
                tenant_id         = _tenant_id,
                customer_id       = _customer_id,
                receiver          = _receiver,
                latitude_origin   = _latitude_origin,
                longitude_origin  = _longitude_origin,
                latitude_destiny  = _latitude_destiny,
                longitude_destiny = _longitude_destiny,
                dist_unit         = _dist_unit,
                dist_scalar       = _dist_scalar,
                last_touch_time = current_moment
            WHERE id = _agreement_id;

        ELSE
            RAISE EXCEPTION 'Invalid agreement identifier: %', _agreement_id;
    END CASE;

    RETURN (_agreement_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_cargo_assignment(
    _assignment_id     UUID,
    _tenant_id         UUID,
    _driver_id         UUID,
    _vehicle_id        UUID,
    _latitude          DOUBLE PRECISION,
    _longitude         DOUBLE PRECISION
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit cargo assignment                                 >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        04/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _assignment_id IS NULL THEN

            INSERT INTO cargo_assignments (
                id,
                tenant_id,
                driver_id,
                vehicle_id,
                latitude_location,
                longitude_location,
                last_touch_time,
                creation_time,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _driver_id,
                _vehicle_id,
                _latitude,
                _longitude,
                current_moment,
                current_moment,
                false
            ) RETURNING id INTO _assignment_id;

        WHEN _assignment_id IS NOT NULL THEN

            UPDATE cargo_assignments
            SET
                tenant_id         = _tenant_id,
                driver_id         = _driver_id,
                vehicle_id        = _vehicle_id,
                latitude_location = _latitude,
                longitude_location = _longitude,
                last_touch_time = current_moment
            WHERE id = _assignment_id;

        ELSE
            RAISE EXCEPTION 'Invalid cargo_assignment identifier: %', _assignment_id;
    END CASE;

    RETURN (_assignment_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;


CREATE OR REPLACE FUNCTION alter_trans_log_record(
    _id                   UUID,
    _tenant_id            UUID,
    _cargo_assignment_id  UUID,
    _dist_unit            VARCHAR,
    _dist_scalar          NUMERIC,
    _fuel_consumption     NUMERIC
) RETURNS RECORD
LANGUAGE plpgsql
AS $$
DECLARE
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    -- >> Description: Create/Edit trans_log_records                                >>
    -- >> Version:     haul                                                         >>
    -- >> Date:        04/may/2025                                                  >>
    -- >> Developer:   Edwin Plauchu for agnux                                      >>
    -- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    current_moment TIMESTAMP WITH TIME ZONE := now();
    rmsg TEXT := '';
BEGIN
    CASE
        WHEN _id IS NULL THEN

            INSERT INTO trans_log_records (
                id,
                tenant_id,
                dist_unit,
                dist_scalar,
                fuel_consumption,
                cargo_assignment_id,
                last_touch_time,
                creation_time
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _dist_unit,
                _dist_scalar,
                _fuel_consumption,
                _cargo_assignment_id,
                current_moment,
                current_moment
            ) RETURNING id INTO _id;

        WHEN _id IS NOT NULL THEN

            UPDATE trans_log_records
            SET
                tenant_id           = _tenant_id,
                dist_unit           = _dist_unit,
                dist_scalar         = _dist_scalar,
                fuel_consumption    = _fuel_consumption,
                cargo_assignment_id = _cargo_assignment_id,
                last_touch_time = current_moment
            WHERE id = _id;

        ELSE
            RAISE EXCEPTION 'Invalid transport log record identifier: %', _id;
    END CASE;

    RETURN (_id::UUID, ''::TEXT);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);

END;
$$;
