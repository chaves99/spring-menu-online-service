CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    establishment_name VARCHAR(150) UNIQUE NOT NULL,
    instagram VARCHAR,
    facebook VARCHAR,
    website VARCHAR,
    phone VARCHAR,
    whatsapp VARCHAR,
    code VARCHAR(20),
    city VARCHAR(100),
    address_line VARCHAR(250),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule(
    id SERIAL PRIMARY KEY,
    days VARCHAR(60),
    open_hour VARCHAR(20),
    close_hour VARCHAR(20),
    start_launch VARCHAR(20),
    end_launch VARCHAR(20),
    user_id INTEGER NOT NULL REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS token_access(
    token VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS categories(
    id SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products(
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(320),
    image VARCHAR,
    active BOOLEAN DEFAULT TRUE,
    category_id INTEGER NOT NULL REFERENCES categories(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prices(
    id BIGSERIAL PRIMARY KEY,
    value DECIMAL(8, 2) NOT NULL,
    unit VARCHAR(30),
    product_id INTEGER NOT NULL REFERENCES products(id)
);

