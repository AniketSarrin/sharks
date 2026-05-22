-- Creator / owner for authorization (organizer may only update/delete own events)
ALTER TABLE events ADD COLUMN organizer_id UUID;

UPDATE events
SET organizer_id = '00000000-0000-0000-0000-000000000000'::uuid
WHERE organizer_id IS NULL;

ALTER TABLE events ALTER COLUMN organizer_id SET NOT NULL;

CREATE INDEX idx_events_organizer_id ON events (organizer_id);
