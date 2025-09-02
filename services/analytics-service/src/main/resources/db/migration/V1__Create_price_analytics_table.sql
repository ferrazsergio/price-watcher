CREATE TABLE IF NOT EXISTS price_analytics (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    date DATE NOT NULL,
    min_price DECIMAL(10,2) NOT NULL,
    max_price DECIMAL(10,2) NOT NULL,
    avg_price DECIMAL(10,2) NOT NULL,
    open_price DECIMAL(10,2) NOT NULL,
    close_price DECIMAL(10,2) NOT NULL,
    check_count INTEGER NOT NULL,
    price_change_amount DECIMAL(10,2),
    price_change_percent DECIMAL(10,4),
    is_working_day BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Unique constraint to prevent duplicate analytics for same product and date
ALTER TABLE price_analytics ADD CONSTRAINT uk_price_analytics_product_date UNIQUE (product_id, date);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_price_analytics_product_id ON price_analytics(product_id);
CREATE INDEX IF NOT EXISTS idx_price_analytics_date ON price_analytics(date);
CREATE INDEX IF NOT EXISTS idx_price_analytics_product_date ON price_analytics(product_id, date DESC);

-- Index for finding recent analytics
CREATE INDEX IF NOT EXISTS idx_price_analytics_created_at ON price_analytics(created_at DESC);

-- Index for price change analysis
CREATE INDEX IF NOT EXISTS idx_price_analytics_price_change ON price_analytics(price_change_percent) 
WHERE price_change_percent IS NOT NULL;