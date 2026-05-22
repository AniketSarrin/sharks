-- Custom access token hook:
-- - Reads `event.claims.app_metadata.role`
-- - Adds `event.claims.user_role` to the issued JWT
--
-- Supabase Auth's built-in `role` claim is preserved (it's required for Supabase/RLS).

create or replace function public.custom_access_token_hook(event jsonb)
returns jsonb
language plpgsql
as $$
declare
	app_role text;
begin
	-- Supabase includes app_metadata in hook input claims.
	app_role := event->'claims'->'app_metadata'->>'role';

	if app_role is not null then
		-- Expose the app role as a dedicated claim for downstream services.
		event := jsonb_set(event, '{claims,user_role}', to_jsonb(app_role), true);
	end if;

	return event;
end;
$$;

