CREATE TABLE category
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)
);

ALTER TABLE product
    DROP COLUMN category;

ALTER TABLE product
    ADD COLUMN category_id BIGINT NOT NULL;

ALTER TABLE product
    ADD CONSTRAINT product_category_id_fk FOREIGN KEY (category_id) REFERENCES category(id);
