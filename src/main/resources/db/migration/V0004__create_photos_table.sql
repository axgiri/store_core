-- Photos table
CREATE TABLE IF NOT EXISTS photos (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  object_key VARCHAR(512) NOT NULL UNIQUE,
  content_type VARCHAR(255) NOT NULL,
  size BIGINT NOT NULL,
  bucket VARCHAR(255) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  person_id BIGINT UNIQUE,
  product_id BIGINT
);

ALTER TABLE photos
  ADD CONSTRAINT fk_photos_person
  FOREIGN KEY (person_id) REFERENCES persons(id)
  ON DELETE CASCADE;

ALTER TABLE photos
    ADD CONSTRAINT fk_photos_product
        FOREIGN KEY (product_id) REFERENCES products(id)
            ON DELETE CASCADE;
