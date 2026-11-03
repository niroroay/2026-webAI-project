
CREATE DATABASE IF NOT EXISTS longcare_db;
USE longcare_db;

CREATE TABLE IF NOT EXISTS care_institutions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_code VARCHAR(50) NOT NULL UNIQUE,
    institution_name VARCHAR(255) NOT NULL,
    institution_type VARCHAR(100),
    city_code VARCHAR(20),
    district_code VARCHAR(20),
    address VARCHAR(500),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    abc_type VARCHAR(20),
    phone VARCHAR(100),
    email VARCHAR(255),
    manager_name VARCHAR(100),
    contract_start DATE,
    contract_end DATE,
    source_updated_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS care_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    contract_city_code VARCHAR(20),
    contract_district VARCHAR(2000),
    contract_start DATE,
    contract_end DATE,
    FOREIGN KEY (institution_id)
        REFERENCES care_institutions(id)
);
