CREATE TABLE contact (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE phone_number (
    id BIGSERIAL PRIMARY KEY,
    contact_id BIGINT REFERENCES contact(id) ON DELETE CASCADE,
    phone_number VARCHAR(20) NOT NULL
);

CREATE TABLE address (
    id BIGSERIAL PRIMARY KEY,
    contact_id BIGINT REFERENCES contact(id) ON DELETE CASCADE,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL
);
