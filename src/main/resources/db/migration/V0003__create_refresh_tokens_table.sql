-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  token_hash VARCHAR(512) NOT NULL UNIQUE,
  person_id BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  replaced_by_token_id BIGINT
);

ALTER TABLE refresh_tokens
  ADD CONSTRAINT fk_refresh_tokens_person
  FOREIGN KEY (person_id) REFERENCES persons(id)
  ON DELETE CASCADE;
