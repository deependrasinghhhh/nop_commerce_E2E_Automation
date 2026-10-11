-- verify_order_placement.sql
-- Traceable to REQ-DB-002 / TC-DB-002
-- Asserts order and order item rows exist in [Order] and OrderItem tables after checkout completion
SELECT 
    o.Id AS OrderId,
    o.OrderGuid,
    o.CustomerId,
    o.OrderStatusId,
    o.PaymentStatusId,
    o.ShippingStatusId,
    o.OrderTotal,
    oi.Id AS OrderItemId,
    oi.ProductId,
    oi.Quantity,
    oi.PriceInclTax
FROM [Order] o
INNER JOIN OrderItem oi ON o.Id = oi.OrderId
WHERE o.Id = :orderId
  AND o.Deleted = 0;
