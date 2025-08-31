-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url VARCHAR(2000) NOT NULL,
    target_price DECIMAL(10,2) NOT NULL,
    current_price DECIMAL(10,2),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    store VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    image_url VARCHAR(2000),
    brand VARCHAR(255),
    model VARCHAR(255),
    selector VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_checked_at TIMESTAMP,
    last_error TEXT,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_user_id ON products(user_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_store ON products(store);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_url ON products(url);

-- Create unique constraint for user_id + url combination
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_user_url ON products(user_id, url) WHERE active = true;