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

CREATE TABLE customers (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId
    name VARCHAR(128) NOT NULL,
);


CREATE TABLE agreements (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId

    customer_id UUID NOT NULL,
    latitude_origin DOUBLE PRECISION NOT NULL,
    longitude_origin DOUBLE PRECISION NOT NULL,
    latitude_destiny DOUBLE PRECISION NOT NULL,
    longitude_destiny DOUBLE PRECISION NOT NULL,

    dist_unit VARCHAR(5) NOT NULL REFERENCES dist_units(code),
    dist_scalar NUMERIC(15, 6) NOT NULL,

    CONSTRAINT unique_route_per_customer UNIQUE (
        customer_id, latitude_origin, longitude_origin, latitude_destiny, longitude_destiny
    )
);


CREATE TABLE patios (
    id UUID PRIMARY KEY,           -- corresponds to TmsBasicModel.Id
    tenant_id UUID NOT NULL,       -- corresponds to TmsBasicModel.tenantId

    latitude_location DOUBLE PRECISION NOT NULL,
    longitude_location DOUBLE PRECISION NOT NULL
);
