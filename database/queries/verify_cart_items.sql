-- verify_cart_items.sql
-- Traceable to REQ-DB-003 / TC-DB-003
-- Asserts shopping cart items reflect updated quantity in ShoppingCartItem table
SELECT 
    sci.Id AS CartItemId,
    sci.CustomerId,
    sci.ProductId,
    sci.Quantity,
    sci.ShoppingCartTypeId,
    sci.CreatedOnUtc,
    sci.UpdatedOnUtc
FROM ShoppingCartItem sci
WHERE sci.CustomerId = :customerId
  AND sci.ProductId = :productId;
