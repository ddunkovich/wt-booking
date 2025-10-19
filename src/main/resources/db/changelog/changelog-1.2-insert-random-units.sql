--liquibase formatted sql
--changeset init:4 splitStatements:false endDelimiter:/
DO $$
DECLARE
    i INT := 0;
    types TEXT[] := ARRAY['HOME', 'FLAT', 'APARTMENTS'];
    type_index INT;
BEGIN
    FOR i IN 1..90 LOOP
            -- корректная индексация: 1, 2 или 3
            type_index := 1 + floor(random() * 3);
    INSERT INTO wtbooking.unit (
        rooms,
        accommodation_type,
        floor,
        is_available,
        cost,
        booking_markup_percent,
        description
    ) VALUES (
                 (1 + floor(random() * 4)),
                 types[type_index],
                 (1 + floor(random() * 10)),
                 TRUE,
                 round((80 + random() * 250)::numeric, 2),
                 round((5 + random() * 20)::numeric, 1),
                 'Auto-generated unit #' || i
             );
END LOOP;
END
$$;
/
