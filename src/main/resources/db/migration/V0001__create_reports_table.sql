-- Reports table
CREATE TABLE IF NOT EXISTS reports (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  reporter_id BIGINT NOT NULL,
  reason VARCHAR(50) NOT NULL,
  details TEXT,
  status VARCHAR(50) NOT NULL,
  moderator_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL,
  resolved_at TIMESTAMPTZ,
  report_type VARCHAR(50) NOT NULL,
  target_id BIGINT NOT NULL
);

ALTER TABLE reports
  ADD CONSTRAINT fk_reports_reporter
  FOREIGN KEY (reporter_id) REFERENCES persons(id)
  ON DELETE CASCADE;

ALTER TABLE reports
  ADD CONSTRAINT fk_reports_moderator
  FOREIGN KEY (moderator_id) REFERENCES persons(id)
  ON DELETE SET NULL;
