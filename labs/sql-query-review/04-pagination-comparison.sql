\echo '--- Offset pagination: later pages still make the database skip work ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 3
  AND status = 'PAID'
ORDER BY created_at DESC, id DESC
OFFSET 5000
LIMIT 50;

\echo '--- Seek pagination: move from the last seen tuple instead of skipping rows ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM orders
WHERE tenant_id = 3
  AND status = 'PAID'
  AND (created_at, id) < (
      SELECT created_at, id
      FROM orders
      WHERE tenant_id = 3
        AND status = 'PAID'
      ORDER BY created_at DESC, id DESC
      OFFSET 5000
      LIMIT 1
  )
ORDER BY created_at DESC, id DESC
LIMIT 50;
