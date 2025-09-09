-- -- Ensure all foreign keys referencing persons(id) have proper ON DELETE behavior
-- -- Note: PostgreSQL does not support altering ON DELETE directly; we drop and recreate constraints.

-- -- Reviews.author_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS reviews
--     DROP CONSTRAINT IF EXISTS fk_reviews_author;
-- ALTER TABLE IF EXISTS reviews
--     ADD CONSTRAINT fk_reviews_author
--     FOREIGN KEY (author_id) REFERENCES persons(id)
--     ON DELETE CASCADE;

-- -- Reviews.person_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS reviews
--     DROP CONSTRAINT IF EXISTS fk_reviews_person;
-- ALTER TABLE IF EXISTS reviews
--     ADD CONSTRAINT fk_reviews_person
--     FOREIGN KEY (person_id) REFERENCES persons(id)
--     ON DELETE CASCADE;

-- -- Reports.reporter_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS reports
--     DROP CONSTRAINT IF EXISTS fk_reports_reporter;
-- ALTER TABLE IF EXISTS reports
--     ADD CONSTRAINT fk_reports_reporter
--     FOREIGN KEY (reporter_id) REFERENCES persons(id)
--     ON DELETE CASCADE;

-- -- Reports.moderator_id -> persons(id) ON DELETE SET NULL
-- ALTER TABLE IF EXISTS reports
--     DROP CONSTRAINT IF EXISTS fk_reports_moderator;
-- ALTER TABLE IF EXISTS reports
--     ADD CONSTRAINT fk_reports_moderator
--     FOREIGN KEY (moderator_id) REFERENCES persons(id)
--     ON DELETE SET NULL;

-- -- Refresh tokens.person_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS refresh_tokens
--     DROP CONSTRAINT IF EXISTS fk_refresh_tokens_person;
-- ALTER TABLE IF EXISTS refresh_tokens
--     ADD CONSTRAINT fk_refresh_tokens_person
--     FOREIGN KEY (person_id) REFERENCES persons(id)
--     ON DELETE CASCADE;

-- -- Photos.person_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS photos
--     DROP CONSTRAINT IF EXISTS fk_photos_person;
-- ALTER TABLE IF EXISTS photos
--     ADD CONSTRAINT fk_photos_person
--     FOREIGN KEY (person_id) REFERENCES persons(id)
--     ON DELETE CASCADE;

-- -- Shops.owner_id -> persons(id) ON DELETE CASCADE
-- ALTER TABLE IF EXISTS shops
--     DROP CONSTRAINT IF EXISTS fk_shops_owner;
-- ALTER TABLE IF EXISTS shops
--     ADD CONSTRAINT fk_shops_owner
--     FOREIGN KEY (owner_id) REFERENCES persons(id)
--     ON DELETE CASCADE;
