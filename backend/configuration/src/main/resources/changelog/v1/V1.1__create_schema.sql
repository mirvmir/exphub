--liquibase formatted sql

--changeset unikitina:2026-02-19-10:00
CREATE SCHEMA IF NOT EXISTS bookstore_db;

--changeset unikitina:2026-02-19-10:05
CREATE TABLE IF NOT EXISTS users (
  user_id           INT PRIMARY KEY,
  email             VARCHAR(200) NOT NULL CONSTRAINT uq_users_email UNIQUE (email),
  password_hash     VARCHAR(255) NOT NULL,
  role              VARCHAR(50) NOT NULL
);
--rollback DROP TABLE customer;

--changeset unikitina:2026-02-19-10:06
CREATE TABLE IF NOT EXISTS book_model (
  isbn              VARCHAR(50) PRIMARY KEY,
  title             VARCHAR(200) NOT NULL,
  author            VARCHAR(200) NOT NULL,
  price             NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  stockCount        INT,
  requestCount      INT
);
--rollback DROP TABLE book_model;

--changeset unikitina:2026-02-19-10:07
CREATE TABLE IF NOT EXISTS book_unit (
  book_unit_id      INT PRIMARY KEY,
  isbn              VARCHAR(50) NOT NULL REFERENCES book_model(isbn),
  delivery_date     DATE NOT NULL,
  available         BOOLEAN NOT NULL
);
--rollback DROP TABLE book_unit;

--changeset unikitina:2026-02-19-10:08
CREATE TABLE IF NOT EXISTS book_lists (
  book_lists_id   INT PRIMARY KEY,
  isbn            VARCHAR(50) NOT NULL REFERENCES book_model(isbn),
  book_unit_id    INT NULL REFERENCES book_unit(book_unit_id)
);
--rollback DROP TABLE book_lists;

--changeset unikitina:2026-02-19-10:09
CREATE TABLE IF NOT EXISTS orders (
  order_id          INT PRIMARY KEY,
  user_id           INT NOT NULL REFERENCES users(user_id),
  status            VARCHAR,
  created_date      TIMESTAMP NOT NULL,
  completion_date   TIMESTAMP NULL,
  total_price       NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total_price >= 0),
  book_lists_id     INT NOT NULL REFERENCES book_lists(book_lists_id) ON DELETE CASCADE
);
--rollback DROP TABLE orders;

--changeset unikitina:2026-02-19-10:10
CREATE TABLE IF NOT EXISTS book_request (
  book_request_id   BIGSERIAL PRIMARY KEY,
  isbn              VARCHAR(50) NOT NULL REFERENCES book_model(isbn),
  user_id           INT NOT NULL REFERENCES users(user_id)
);
--rollback DROP TABLE book_request;
