DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    segment TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    status TEXT NOT NULL,
    total_yen NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    sku TEXT NOT NULL,
    quantity INT NOT NULL,
    unit_price_yen NUMERIC(12, 2) NOT NULL
);

INSERT INTO customers (tenant_id, email, segment, created_at)
SELECT
    ((g - 1) % 5) + 1,
    'customer' || g || '@example.com',
    CASE
        WHEN g % 3 = 0 THEN 'vip'
        WHEN g % 3 = 1 THEN 'standard'
        ELSE 'new'
    END,
    NOW() - (g || ' hours')::interval
FROM generate_series(1, 2000) AS g;

INSERT INTO orders (tenant_id, customer_id, status, total_yen, created_at)
SELECT
    c.tenant_id,
    c.id,
    CASE
        WHEN g % 10 < 5 THEN 'PAID'
        WHEN g % 10 < 7 THEN 'PENDING'
        WHEN g % 10 < 9 THEN 'FAILED'
        ELSE 'CANCELLED'
    END,
    (50 + (g % 9000))::numeric(12, 2),
    NOW() - (g || ' minutes')::interval
FROM generate_series(1, 50000) AS g
JOIN customers c ON c.id = ((g - 1) % 2000) + 1;

INSERT INTO order_items (order_id, sku, quantity, unit_price_yen)
SELECT
    o.id,
    'SKU-' || ((g % 120) + 1),
    ((g % 4) + 1),
    (100 + (g % 5000))::numeric(12, 2)
FROM generate_series(1, 150000) AS g
JOIN orders o ON o.id = ((g - 1) % 50000) + 1;

ANALYZE;
