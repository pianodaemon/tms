CREATE OR REPLACE FUNCTION public.alter_vehicle(
    _vehicle_id UUID,
    _tenant_id UUID,
    _number_plate VARCHAR,
    _vehicle_type VARCHAR,
    _perf_dist_unit VARCHAR,
    _perf_vol_unit VARCHAR,
    _perf_vol_scalar NUMERIC,
    _perf_scalar NUMERIC
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
    new_id UUID;
BEGIN
    CASE
        WHEN _vehicle_id IS NULL THEN
            -- Insert new vehicle
            INSERT INTO vehicles (
                id,
                tenant_id,
                number_plate,
                vehicle_type,
                perf_dist_unit,
                perf_vol_unit,
                perf_vol_scalar,
                perf_scalar
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _number_plate,
                _vehicle_type,
                _perf_dist_unit,
                _perf_vol_unit,
                _perf_vol_scalar,
                _perf_scalar
            )
            RETURNING id INTO new_id;

            RETURN (new_id::UUID, ''::TEXT);

        WHEN _vehicle_id IS NOT NULL THEN
            -- Update existing vehicle
            UPDATE vehicles
            SET tenant_id       = _tenant_id,
                number_plate    = _number_plate,
                vehicle_type    = _vehicle_type,
                perf_dist_unit  = _perf_dist_unit,
                perf_vol_unit   = _perf_vol_unit,
                perf_vol_scalar = _perf_vol_scalar,
                perf_scalar     = _perf_scalar
            WHERE id = _vehicle_id;

            RETURN (_vehicle_id::UUID, ''::TEXT);

        ELSE
            RAISE EXCEPTION 'Unexpected condition for _vehicle_id: %', _vehicle_id;
    END CASE;

EXCEPTION
    WHEN unique_violation THEN
        RETURN (NULL::UUID, format('Vehicle with number plate %s already exists for tenant %s', _number_plate, _tenant_id));

    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS rmsg = MESSAGE_TEXT;
        RETURN (NULL::UUID, rmsg::TEXT);
END;
$$;
