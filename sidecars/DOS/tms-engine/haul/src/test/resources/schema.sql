--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.5
-- Dumped by pg_dump version 9.6.20

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

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;

--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

--
-- Enable pgcrypto extension (needed for gen_random_uuid)
--

CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TABLE customers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL
);


CREATE TABLE drivers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    license_number VARCHAR(128) NOT NULL,
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
    CONSTRAINT unique_route_per_customer UNIQUE (
        customer_id, latitude_origin, longitude_origin, latitude_destiny, longitude_destiny
    )
);


CREATE TABLE patios (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
    latitude_location DOUBLE PRECISION NOT NULL,
    longitude_location DOUBLE PRECISION NOT NULL
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
