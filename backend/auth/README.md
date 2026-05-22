# Auth Service - Supabase JWT Role Integration

This service provisions Supabase Auth users and ensures your application role is carried through into issued Supabase access tokens.

## 1) Where roles are set

When the Auth service receives `auth.user.created`, it calls the GoTrue admin API to create the Supabase Auth user and stores an application role in:

`app_metadata.role`

Canonical roles from `com.gen.auth.model.AppRole` are written directly to auth metadata:

- `ATTENDEE` -> `attendee`
- `ADMIN` -> `admin`
- `ORGANIZER` -> `organizer`

Implementation:

- `backend/auth/src/main/java/com/gen/auth/service/AuthService.java`
- `backend/auth/src/main/java/com/gen/auth/client/GotrueAdminCreateUserRequest.java`

## 2) JWT claims you should expect

Supabase requires its own JWT `role` claim for Supabase/RLS behavior. This integration preserves Supabase's standard `role` claim (commonly `authenticated`).

After the role integration is enabled, your application can use:

- `app_metadata.role`: `attendee | admin | organizer`
- `user_role`: `attendee | admin | organizer`

Example (decoded JWT excerpt):

```json
{
  "role": "authenticated",
  "app_metadata": {
    "role": "attendee"
  },
  "user_role": "attendee"
}
```

## 3) Supabase schema + seed migrations

Role enum and role rows are initialized with canonical values only:

- `backend/auth/supabase/migrations/20260101000001_init_app_role_and_user_roles.sql`
- `backend/auth/supabase/migrations/20260101000002_user_roles_rls.sql`
- `backend/auth/supabase/migrations/20260101000003_seed_user_roles.sql`

`public.app_role` values:

- `attendee`
- `admin`
- `organizer`

Dev seed (`20260101000003`) inserts only canonical role values, matching `AppRole` and JWT metadata.

## 4) Supabase SQL hook added by this repo

Migration:

- `backend/auth/supabase/migrations/20260101000004_custom_access_token_hook.sql`

Function created:

- `public.custom_access_token_hook(event jsonb)`

Behavior:

- Reads `event.claims.app_metadata.role`
- Writes `event.claims.user_role` (so downstream services can read `user_role`)

## 5) Enable the custom access token hook in Supabase

Enabling the hook itself is done in the Supabase Auth dashboard (it is not fully represented by the SQL migration alone).

In Supabase Dashboard:

1. Go to `Authentication` -> `Hooks`
2. Select `Custom Access Token`
3. Enable the hook and point it to the SQL function `public.custom_access_token_hook`

After enabling, logging in through this service will return access tokens that include `user_role`.

## 6) How to test

1. Provision a user via your normal `auth.user.created` flow.
2. Call this service login:
   - `POST /api/v1/auth/login`
3. Copy the returned `accessToken`
4. Decode the JWT and verify:
   - `app_metadata.role` exists and matches your selected role
   - `user_role` exists and matches your selected role

