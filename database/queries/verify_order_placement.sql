-- verify_order_placement.sql
-- Traceable to REQ-DB-002 / TC-DB-002
-- Counts persisted active orders that have at least one OrderItem in the seeded verification database.
SELECT 
    COUNT(DISTINCT o.Id) AS ActiveOrdersWithItems
FROM [Order] o
INNER JOIN OrderItem oi ON o.Id = oi.OrderId
WHERE o.Deleted = 0;
