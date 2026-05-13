--liquibase formatted sql

--changeset unikitina:2026-03-19-10:05
CREATE TABLE IF NOT EXISTS refresh_token (
  refresh_token_id  INT PRIMARY KEY,
  user_id           INT NOT NULL REFERENCES users(user_id),
  token_hash        VARCHAR(200) NOT NULL,
  created_at        TIMESTAMP NOT NULL,
  expires_at        TIMESTAMP NOT NULL
);
--rollback DROP TABLE refresh_token;