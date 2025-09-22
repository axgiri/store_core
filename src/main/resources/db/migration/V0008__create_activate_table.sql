-- Activate table
CREATE TABLE IF NOT EXISTS active (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  otp INT NOT NULL,
  is_active BOOLEAN NOT NULL,
  is_login_attempted BOOLEAN,
  created_at TIMESTAMPTZ NOT NULL,
  otp_reset INT
);
