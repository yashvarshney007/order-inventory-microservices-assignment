 

-- Sample data for testing Inventory Service 

-- Insert Products 

INSERT INTO products (id, product_code, name, description) VALUES (1, 'PROD-001', 'Widget A', 'High quality widget'); 

INSERT INTO products (id, product_code, name, description) VALUES (2, 'PROD-002', 'Gadget B', 'Premium gadget'); 

INSERT INTO products (id, product_code, name, description) VALUES (3, 'PROD-003', 'Tool C', 'Professional tool'); 

-- Insert Batches for PROD-001 (multiple batches with different expiry dates) 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (1, 'BATCH-001-A', 1, 100, '2025-12-31', '2025-01-15'); 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (2, 'BATCH-001-B', 1, 150, '2026-06-30', '2025-02-20'); 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (3, 'BATCH-001-C', 1, 200, '2025-09-15', '2025-03-10'); 

-- Insert Batches for PROD-002 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (4, 'BATCH-002-A', 2, 75, '2025-11-30', '2025-01-20'); 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (5, 'BATCH-002-B', 2, 125, '2026-03-31', '2025-02-15'); 

-- Insert Batches for PROD-003 

INSERT INTO batches (id, batch_number, product_id, quantity, expiry_date, received_date)  

VALUES (6, 'BATCH-003-A', 3, 50, '2025-08-31', '2025-01-10'); 