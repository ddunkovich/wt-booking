-- create db
CREATE DATABASE wtbooking
    WITH OWNER = root
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- create user
CREATE USER wtuser WITH PASSWORD 'secret';

-- grant privileges
GRANT ALL PRIVILEGES ON DATABASE wtbooking TO wtuser;

-- connect to db and enable uuid extension
\connect wtbooking;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS wtbooking AUTHORIZATION wtuser;
