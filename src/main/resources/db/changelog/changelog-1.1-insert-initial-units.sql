-- liquibase formatted sql
-- changeset init:3
INSERT INTO wtbooking.unit (rooms, accommodation_type, floor, is_available, cost, booking_markup_percent, description)
VALUES
    (1, 'FLAT', 2, TRUE, 120.0, 10.0, 'Cozy flat for two'),
    (2, 'FLAT', 3, TRUE, 160.0, 12.0, 'Modern flat with balcony'),
    (3, 'APARTMENTS', 5, TRUE, 250.0, 15.0, 'Spacious family apartment'),
    (4, 'HOME', 1, TRUE, 300.0, 20.0, 'Private home with garden'),
    (2, 'HOME', 2, TRUE, 180.0, 10.0, 'Small house in suburbs'),
    (3, 'APARTMENTS', 4, TRUE, 220.0, 12.0, 'City-view apartment'),
    (1, 'FLAT', 1, TRUE, 100.0, 8.0, 'Budget flat near metro'),
    (2, 'HOME', 1, TRUE, 200.0, 10.0, 'Country home near lake'),
    (3, 'APARTMENTS', 6, TRUE, 270.0, 18.0, 'Penthouse'),
    (2, 'FLAT', 5, TRUE, 150.0, 9.0, 'Flat with large kitchen');

