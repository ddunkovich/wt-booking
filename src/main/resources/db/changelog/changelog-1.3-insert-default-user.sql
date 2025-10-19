--liquibase formatted sql

--changeset wt:insert-default-user
INSERT INTO app_user (id, username, email)
VALUES ('00000000-0000-0000-0000-000000000000', 'default_user', 'default@example.com')
ON CONFLICT (id) DO NOTHING;