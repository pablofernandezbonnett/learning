\echo '--- Baseline list query without a supporting composite index ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 3
  AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 50;

\echo '--- Join query worth checking before tuning ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT o.id, o.status, c.segment
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.tenant_id = 3
  AND o.status = 'PAID'
ORDER BY o.created_at DESC
LIMIT 50;
