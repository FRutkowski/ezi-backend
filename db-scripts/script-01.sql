CREATE TABLE product
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50),
    price       SMALLINT,
    photo       BYTEA,
    category    VARCHAR(50)
);

CREATE TABLE client
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(50),
    last_name   VARCHAR(50),
    address     VARCHAR(50)
);

CREATE TABLE order_table
(
    id          BIGSERIAL PRIMARY KEY,
    final_price SMALLINT,
    date        TIMESTAMP,
    client_id   BIGINT NOT NULL,
    CONSTRAINT order_client_id_fk FOREIGN KEY (client_id) REFERENCES client(id)
);

CREATE TABLE order_product
(
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    CONSTRAINT order_product_order_id_fk FOREIGN KEY (order_id) REFERENCES order_table(id),
    CONSTRAINT order_product_product_id_fk FOREIGN KEY (product_id) REFERENCES product(id)
);
