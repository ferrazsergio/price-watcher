CREATE TABLE IF NOT EXISTS price_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    previous_price DECIMAL(10,2),
    source VARCHAR(100) NOT NULL,
    currency VARCHAR(10) DEFAULT 'BRL',
    available BOOLEAN DEFAULT TRUE,
    error TEXT,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    raw_data TEXT
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_price_history_product_id ON price_history(product_id);
CREATE INDEX IF NOT EXISTS idx_price_history_checked_at ON price_history(checked_at);
CREATE INDEX IF NOT EXISTS idx_price_history_product_checked ON price_history(product_id, checked_at DESC);

-- Index for finding price changes
CREATE INDEX IF NOT EXISTS idx_price_history_changes ON price_history(checked_at, previous_price, price) 
WHERE previous_price IS NOT NULL;