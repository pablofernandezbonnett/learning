\echo '--- Add one composite index that matches the hot list query ---'
CREATE INDEX idx_orders_tenant_status_created
ON orders (tenant_id, status, created_at DESC, id DESC);

ANALYZE orders;

\echo '--- Same list query after the index ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 3
  AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 50;

\echo '--- Same join query after the index ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT o.id, o.status, c.segment
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.tenant_id = 3
  AND o.status = 'PAID'
ORDER BY o.created_at DESC
LIMIT 50;
