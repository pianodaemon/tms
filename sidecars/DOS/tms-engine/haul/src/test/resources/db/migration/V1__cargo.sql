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


CREATE TABLE customers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    blocked boolean DEFAULT false NOT NULL,
    name VARCHAR(128) NOT NULL
);


CREATE TABLE drivers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    license_number VARCHAR(128) NOT NULL,
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT unique_license_per_tenant UNIQUE (tenant_id, license_number)
);


CREATE TABLE vehicles (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    number_plate VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,  -- Store as a string (enum values)
    perf_dist_unit VARCHAR(50),         -- Store as a string (enum values)
    perf_vol_unit VARCHAR(50),          -- Store as a string (enum values)
    perf_scalar NUMERIC(10, 2),
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT vehicle_unique_number_plate UNIQUE (tenant_id, number_plate)
);


CREATE TABLE agreements (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    customer_id UUID REFERENCES customers(id),
    latitude_origin DOUBLE PRECISION NOT NULL,
    longitude_origin DOUBLE PRECISION NOT NULL,
    latitude_destiny DOUBLE PRECISION NOT NULL,
    longitude_destiny DOUBLE PRECISION NOT NULL,
    dist_unit VARCHAR(5) NOT NULL,
    dist_scalar NUMERIC(15, 6) NOT NULL,
    blocked boolean DEFAULT false NOT NULL,
    CONSTRAINT unique_route_per_customer UNIQUE (
        customer_id, latitude_origin, longitude_origin, latitude_destiny, longitude_destiny
    )
);


CREATE TABLE patios (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    latitude_location DOUBLE PRECISION NOT NULL,
    longitude_location DOUBLE PRECISION NOT NULL,
    blocked boolean DEFAULT false NOT NULL
);


CREATE TABLE trans_log_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    trans_log_record_id VARCHAR(255),  -- corresponds to TmsBasicModel.Id
    dist_unit VARCHAR(5) NOT NULL,
    dist_scalar NUMERIC(15, 6) NOT NULL,
    fuel_consumption NUMERIC(10, 2)
);


CREATE TABLE cargo_assignment (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    driver_id UUID REFERENCES drivers(id),
    vehicle_id UUID REFERENCES vehicles(id),
    trans_log_record_id UUID REFERENCES trans_log_records(id),
    latitude_location DOUBLE PRECISION,
    longitude_location DOUBLE PRECISION
);


CREATE OR REPLACE FUNCTION alter_vehicle(
    _vehicle_id        UUID,
    _tenant_id         UUID,
    _number_plate      VARCHAR,
    _vehicle_type      VARCHAR,
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
                vehicle_type,
                perf_dist_unit,
                perf_vol_unit,
                perf_scalar,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _number_plate,
                _vehicle_type,
                _perf_dist_unit,
                _perf_vol_unit,
                _perf_scalar,
                false
            ) RETURNING id INTO _vehicle_id;

        WHEN _vehicle_id IS NOT NULL THEN

            UPDATE vehicles
            SET
                tenant_id      = _tenant_id,
                number_plate   = _number_plate,
                vehicle_type   = _vehicle_type,
                perf_dist_unit = _perf_dist_unit,
                perf_vol_unit  = _perf_vol_unit,
                perf_scalar    = _perf_scalar
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
                license_number,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                _license_number,
                false
            ) RETURNING id INTO _driver_id;

        WHEN _driver_id IS NOT NULL THEN

            UPDATE drivers
            SET
                tenant_id      = _tenant_id,
                name           = _name,
                license_number = _license_number
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
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                _latitude,
                _longitude,
                false
            ) RETURNING id INTO _patio_id;

        WHEN _patio_id IS NOT NULL THEN

            UPDATE patios
            SET
                tenant_id         = _tenant_id,
                name              = _name,
                latitude_location = _latitude,
                longitude_location = _longitude
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
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _name,
                false
            ) RETURNING id INTO _customer_id;

        WHEN _customer_id IS NOT NULL THEN

            UPDATE customers
            SET
                tenant_id = _tenant_id,
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
                latitude_origin,
                longitude_origin,
                latitude_destiny,
                longitude_destiny,
                dist_unit,
                dist_scalar,
                blocked
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _customer_id,
                _latitude_origin,
                _longitude_origin,
                _latitude_destiny,
                _longitude_destiny,
                _dist_unit,
                _dist_scalar,
                false
            ) RETURNING id INTO _agreement_id;

        WHEN _agreement_id IS NOT NULL THEN

            UPDATE agreements
            SET
                tenant_id         = _tenant_id,
                customer_id       = _customer_id,
                latitude_origin   = _latitude_origin,
                longitude_origin  = _longitude_origin,
                latitude_destiny  = _latitude_destiny,
                longitude_destiny = _longitude_destiny,
                dist_unit         = _dist_unit,
                dist_scalar       = _dist_scalar
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
