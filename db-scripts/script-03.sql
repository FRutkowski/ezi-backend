CREATE TABLE client_action
(
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT NOT NULL,
    object_id   BIGINT NOT NULL,
    type        VARCHAR(50),
    date      TIMESTAMP,
    CONSTRAINT client_action_client_id_fk FOREIGN KEY (client_id) REFERENCES client(id)
);