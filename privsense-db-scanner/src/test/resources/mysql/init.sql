-- Test schema for MySQL integration tests

-- Users table with various data types and potential PII
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    birth_date DATE,
    address VARCHAR(255),
    city VARCHAR(50),
    country VARCHAR(50),
    zip_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    login_count INT DEFAULT 0,
    salary DECIMAL(10, 2),
    notes TEXT
);

-- Create an orders table that has a foreign key relationship with users
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(255),
    tracking_number VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create a products table
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    category VARCHAR(50),
    image_url VARCHAR(255),
    is_featured BOOLEAN DEFAULT FALSE
);

-- Create a order_items junction table
CREATE TABLE order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Insert some test data into the users table
INSERT INTO users (first_name, last_name, email, phone_number, birth_date, address, city, country, zip_code)
VALUES 
('John', 'Doe', 'john.doe@example.com', '555-123-4567', '1980-01-15', '123 Main St', 'Springfield', 'USA', '12345'),
('Jane', 'Smith', 'jane.smith@example.com', '555-987-6543', '1985-05-20', '456 Oak Ave', 'Rivertown', 'USA', '67890'),
('Alice', 'Johnson', 'alice.j@example.com', '555-555-1212', '1990-10-10', '789 Pine Rd', 'Lakeville', 'USA', '54321');

-- Insert some products
INSERT INTO products (name, description, price, stock_quantity, category)
VALUES 
('Laptop', 'High-performance laptop for professionals', 1299.99, 15, 'Electronics'),
('Smartphone', 'Latest model with advanced camera', 899.99, 25, 'Electronics'),
('Coffee Maker', 'Programmable coffee machine', 49.99, 30, 'Kitchen Appliances');

-- Insert some orders and order items
INSERT INTO orders (user_id, total_amount, shipping_address, status)
VALUES 
(1, 1299.99, '123 Main St, Springfield, USA', 'completed'),
(2, 949.98, '456 Oak Ave, Rivertown, USA', 'shipped'),
(3, 49.99, '789 Pine Rd, Lakeville, USA', 'processing');

-- Add items to the orders
INSERT INTO order_items (order_id, product_id, quantity, price_per_unit)
VALUES 
(1, 1, 1, 1299.99),  -- John bought a laptop
(2, 2, 1, 899.99),   -- Jane bought a smartphone
(2, 3, 1, 49.99),    -- Jane also bought a coffee maker
(3, 3, 1, 49.99);    -- Alice bought a coffee maker
