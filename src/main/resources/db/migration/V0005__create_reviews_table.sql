-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  author_id BIGINT NOT NULL,
  rating BIGINT,
  person_id BIGINT,
  comment TEXT
);

ALTER TABLE reviews
  ADD CONSTRAINT fk_reviews_author
  FOREIGN KEY (author_id) REFERENCES persons(id)
  ON DELETE CASCADE;

ALTER TABLE reviews
  ADD CONSTRAINT fk_reviews_person
  FOREIGN KEY (person_id) REFERENCES persons(id)
  ON DELETE CASCADE;
