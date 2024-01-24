-- WITH product_order_combinations AS (
--     SELECT DISTINCT
--         generate_series(1, 10) AS order_id,
--         ROUND(RANDOM() * 24) + 1 AS product_id
--     FROM generate_series(1, 200)
-- )
--
-- INSERT INTO order_product (order_id, product_id)
-- SELECT * FROM product_order_combinations;
--

INSERT INTO order_product (order_id, product_id)
VALUES
 (1, 12), (1, 5), (1, 8), (1, 13), (1, 21),
 (2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
 (3, 12), (3, 2), (3, 3), (3, 4), (3, 5),
 (4, 12), (4, 14), (4, 5), (4, 18), (4, 20),
 (5, 12), (5, 5), (5, 6), (5, 8), (5, 10),
 (6, 5), (6, 4), (6, 7), (6, 10), (6, 13),
 (6, 12), (6, 7), (6, 5), (6, 15), (6, 19),
 (7, 12), (7, 5), (7, 22), (7, 20), (7, 9),
 (8, 5), (8, 10), (8, 15), (8, 20), (8, 25),
 (9, 14), (9, 8), (9, 19), (9, 2), (9, 12),
 (10, 23), (10, 7), (10, 15), (10, 4), (10, 20);
