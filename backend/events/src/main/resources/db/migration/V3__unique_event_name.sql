-- Event names are unique case-insensitively (matches search behavior on LOWER(name))
DROP INDEX IF EXISTS idx_events_name_lower;
CREATE UNIQUE INDEX uq_events_name_lower ON events (LOWER(name));
