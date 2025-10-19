-- Persons table
CREATE TABLE IF NOT EXISTS persons (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(255) UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role_enum VARCHAR(50),
  is_active BOOLEAN NOT NULL,
  is_not_blocked BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_persons_phone ON persons(phone_number);
CREATE UNIQUE INDEX IF NOT EXISTS ux_persons_email ON persons(email);
