-- Backfill role-specific profile extensions for users that exist in `profiles`
-- but have no row in their role's extension table.
--
-- Why this is needed:
--   V2 introduced attendee_profiles / organizer_profiles / admin_profiles, but did
--   not backfill rows for users that already existed in `profiles`. Only newly
--   registered users (via AuthService.register) get an extension row, so any user
--   created before V2 ran -- or provisioned through a path that bypasses register
--   (e.g. directly via the auth service) -- has no extension row, which makes the
--   role-scoped endpoints (/api/v1/{admins,organizers,attendees}/me) throw
--   UserNotFoundException ("...profile extension not found") -> HTTP 404.
--
-- The NOT EXISTS guard keeps this idempotent; re-running on a healthy DB is a no-op.

INSERT INTO attendee_profiles (id, user_id, created_at, updated_at)
SELECT gen_random_uuid(), p.id, NOW(), NOW()
FROM profiles p
WHERE p.role = 'attendee'
  AND NOT EXISTS (
      SELECT 1 FROM attendee_profiles ap WHERE ap.user_id = p.id
  );

INSERT INTO organizer_profiles (id, user_id, created_at, updated_at)
SELECT gen_random_uuid(), p.id, NOW(), NOW()
FROM profiles p
WHERE p.role = 'organizer'
  AND NOT EXISTS (
      SELECT 1 FROM organizer_profiles op WHERE op.user_id = p.id
  );

INSERT INTO admin_profiles (id, user_id, created_at, updated_at)
SELECT gen_random_uuid(), p.id, NOW(), NOW()
FROM profiles p
WHERE p.role = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM admin_profiles a WHERE a.user_id = p.id
  );
