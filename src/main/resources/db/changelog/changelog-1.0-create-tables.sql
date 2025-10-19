-- liquibase formatted sql

-- changeset wt:unit
CREATE TABLE IF NOT EXISTS wtbooking.unit (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      rooms INT NOT NULL,
                      accommodation_type VARCHAR(20) NOT NULL,
                      floor INT NOT NULL,
                      is_available BOOLEAN NOT NULL,
                      cost NUMERIC(10,2) NOT NULL,
                      booking_markup_percent NUMERIC(10,2) NOT NULL,
                      description TEXT
);

-- changeset wt:app_user
CREATE TABLE IF NOT EXISTS wtbooking.app_user (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         username VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL
);

-- changeset wt:booking
CREATE TABLE IF NOT EXISTS wtbooking.booking (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         unit_id UUID NOT NULL REFERENCES unit(id),
                         user_id UUID NOT NULL REFERENCES app_user(id),
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         total_cost NUMERIC(10,2) NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
