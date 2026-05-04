CREATE TABLE stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_symbol VARCHAR(50) NOT NULL UNIQUE,
    price DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    last_updated TIMESTAMP
);
