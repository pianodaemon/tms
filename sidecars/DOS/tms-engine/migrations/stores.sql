CREATE OR REPLACE FUNCTION public.alter_vehicle(
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
                perf_scalar
            ) VALUES (
                gen_random_uuid(),
                _tenant_id,
                _number_plate,
                _vehicle_type,
                _perf_dist_unit,
                _perf_vol_unit,
                _perf_scalar
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
