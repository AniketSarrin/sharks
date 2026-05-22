-- Optional age range applies to all event types (MUSIC, NETWORKING, DATING).
ALTER TABLE events ADD COLUMN min_age INTEGER;
ALTER TABLE events ADD COLUMN max_age INTEGER;

UPDATE events e
SET min_age = d.min_age,
    max_age = d.max_age
FROM dating_events d
WHERE e.id = d.event_id;

ALTER TABLE dating_events DROP COLUMN min_age;
ALTER TABLE dating_events DROP COLUMN max_age;
