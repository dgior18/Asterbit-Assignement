-- Default admin user (password: 'admin' - BCrypt encoded)
INSERT INTO users (email, password, role, create_date, update_date)
VALUES ('admin@tasktracker.com', '$2a$10$rY.U4U0ij7NIEfO3YO1jxOUz.oThX3RUPqJFpPnL8EBQnJOPzUHtO', 'ADMIN', NOW(), NOW()); 