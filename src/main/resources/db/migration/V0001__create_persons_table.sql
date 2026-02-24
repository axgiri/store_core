-- Persons table
CREATE TABLE IF NOT EXISTS persons (
  id UUID PRIMARY KEY,
  version BIGINT,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(255) UNIQUE,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_persons_phone ON persons(phone_number);
