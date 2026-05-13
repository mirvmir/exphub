--liquibase formatted sql

--changeset unikitina:2026-02-19-10:00
CREATE INDEX IF NOT EXISTS idx_book_unit_isbn ON book_unit(isbn);
CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);

CREATE INDEX IF NOT EXISTS idx_req_customer_isbn ON book_request(customer_id, isbn);
--rollback DROP INDEX IF EXISTS idx_book_unit_isbn;
--rollback DROP INDEX IF EXISTS idx_order_customer;
--rollback DROP INDEX IF EXISTS idx_req_customer_isbn;
