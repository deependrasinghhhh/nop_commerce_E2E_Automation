-- verify_customer_registration.sql
-- Traceable to REQ-DB-001 / TC-DB-001
-- Asserts customer record persists in nopCommerce Customer table after storefront registration
SELECT 
    Id,
    CustomerGuid,
    Username,
    Email,
    Active,
    Deleted,
    CreatedOnUtc
FROM Customer
WHERE Email = :email
  AND Deleted = 0;
