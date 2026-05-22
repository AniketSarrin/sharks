import type {
  BackendUserRole,
  RoleProfileDto,
  RoleProfilePatchBody,
  UserMeDto,
} from "./api-types";
import { normalizeBackendUserRole, roleProfilePathSegment } from "./api-types";
import { clearSession, getAccessToken, getStoredRole } from "./auth-storage";
import { apiFetch, extractApiErrorMessage } from "./http";

type Ok<T> = { success: true; data: T };
type Err = { success: false; error: string };
type Result<T> = Ok<T> | Err;

async function parseJsonResponse<T>(res: Response): Promise<Result<T>> {
  if (res.status === 204) {
    return { success: true, data: undefined as T };
  }
  if (!res.ok) {
    return { success: false, error: await extractApiErrorMessage(res) };
  }
  const ct = res.headers.get("Content-Type") ?? "";
  if (!ct.includes("json")) {
    return { success: false, error: "Unexpected non-JSON response" };
  }
  const body = (await res.json()) as T;
  return { success: true, data: body };
}

function coerceUserMeDto(raw: unknown): UserMeDto | null {
  if (raw == null || typeof raw !== "object") return null;
  const o = raw as Record<string, unknown>;
  const id = o.id;
  const email = o.email;
  const fullName =
    typeof o.fullName === "string" ? o.fullName : o.fullName == null ? "" : String(o.fullName);
  const roleRaw = o.role;
  if (typeof id !== "string" || typeof email !== "string") return null;
  const role = typeof roleRaw === "string" ? normalizeBackendUserRole(roleRaw) : null;
  if (!role) return null;
  return {
    id,
    email,
    fullName,
    avatarUrl: typeof o.avatarUrl === "string" ? o.avatarUrl : (o.avatarUrl == null ? null : String(o.avatarUrl)),
    role,
    bio: typeof o.bio === "string" ? o.bio : (o.bio == null ? null : String(o.bio)),
    phone: typeof o.phone === "string" ? o.phone : (o.phone == null ? null : String(o.phone)),
    location: typeof o.location === "string" ? o.location : (o.location == null ? null : String(o.location)),
    active: Boolean(o.active),
    createdAt: typeof o.createdAt === "string" ? o.createdAt : String(o.createdAt ?? ""),
    updatedAt: typeof o.updatedAt === "string" ? o.updatedAt : String(o.updatedAt ?? ""),
  };
}

function coerceRoleProfileDto(raw: unknown): RoleProfileDto | null {
  const base = coerceUserMeDto(raw);
  if (!base) return null;
  const o = raw as Record<string, unknown>;
  return {
    ...base,
    nickname: o.nickname == null ? undefined : typeof o.nickname === "string" ? o.nickname : String(o.nickname),
    organizerDescription:
      o.organizerDescription == null
        ? undefined
        : typeof o.organizerDescription === "string"
          ? o.organizerDescription
          : String(o.organizerDescription),
  };
}

/** `GET /api/v1/users/me` */
export async function getCurrentUserBase(): Promise<Result<UserMeDto>> {
  const res = await apiFetch("/api/v1/users/me", { method: "GET" });
  const parsed = await parseJsonResponse<unknown>(res);
  if (!parsed.success) return parsed;
  const dto = coerceUserMeDto(parsed.data);
  if (!dto) return { success: false, error: "Invalid user profile response" };
  return { success: true, data: dto };
}

/** `GET /api/v1/{attendees|organizers|admins}/me` */
export async function getMyRoleProfile(role: BackendUserRole): Promise<Result<RoleProfileDto>> {
  const seg = roleProfilePathSegment(role);
  const res = await apiFetch(`/api/v1/${seg}/me`, { method: "GET" });
  const parsed = await parseJsonResponse<unknown>(res);
  if (!parsed.success) return parsed;
  const dto = coerceRoleProfileDto(parsed.data);
  if (!dto) return { success: false, error: "Invalid role profile response" };
  return { success: true, data: dto };
}

/** `PATCH /api/v1/{attendees|organizers|admins}/me` */
export async function patchMyRoleProfile(
  role: BackendUserRole,
  body: RoleProfilePatchBody
): Promise<Result<RoleProfileDto>> {
  const seg = roleProfilePathSegment(role);
  const res = await apiFetch(`/api/v1/${seg}/me`, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
  const parsed = await parseJsonResponse<unknown>(res);
  if (!parsed.success) return parsed;
  const dto = coerceRoleProfileDto(parsed.data);
  if (!dto) return { success: false, error: "Invalid role profile response" };
  return { success: true, data: dto };
}

/**
 * `DELETE /api/v1/{attendees|organizers|admins}/me` — clears local session on success.
 * Caller should redirect after this succeeds.
 */
export async function deleteMyRoleProfile(role: BackendUserRole): Promise<Result<void>> {
  const seg = roleProfilePathSegment(role);
  const res = await apiFetch(`/api/v1/${seg}/me`, { method: "DELETE" });
  if (!res.ok) {
    return { success: false, error: await extractApiErrorMessage(res) };
  }
  clearSession();
  return { success: true, data: undefined };
}

/**
 * Load the full role profile.
 * If a role was stored at login time (from GoTrue app_metadata), we skip the
 * base `GET /api/v1/users/me` call and go straight to the role-specific endpoint,
 * saving a round-trip. Falls back to the two-request flow when no stored role exists.
 */
export async function loadFullProfile(): Promise<Result<RoleProfileDto>> {
  const storedRole = getStoredRole();
  if (storedRole) {
    return getMyRoleProfile(storedRole);
  }
  const base = await getCurrentUserBase();
  if (!base.success) return base;
  return getMyRoleProfile(base.data.role);
}

export interface PublicOrganizerDto {
  id: string;
  fullName: string;
  avatarUrl: string | null;
  bio: string | null;
  organizerDescription: string | null;
}

/** `GET /api/v1/organizers/{id}` — public endpoint, no auth required. */
export async function getOrganizerById(id: string): Promise<Result<PublicOrganizerDto>> {
  const res = await apiFetch(`/api/v1/organizers/${id}`, { method: "GET" });
  const parsed = await parseJsonResponse<unknown>(res);
  if (!parsed.success) return parsed;
  const o = parsed.data as Record<string, unknown>;
  return {
    success: true,
    data: {
      id: String(o.id ?? ""),
      fullName: String(o.fullName ?? ""),
      avatarUrl: o.avatarUrl != null ? String(o.avatarUrl) : null,
      bio: o.bio != null ? String(o.bio) : null,
      organizerDescription: o.organizerDescription != null ? String(o.organizerDescription) : null,
    },
  };
}

/** Best-effort `email_confirmed` from JWT access token (e.g. Supabase/GoTrue). */
export function getEmailConfirmedFromAccessToken(): boolean | null {
  const token = getAccessToken();
  if (!token) return null;
  const parts = token.split(".");
  if (parts.length < 2) return null;
  try {
    const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const pad = b64.length % 4 === 0 ? "" : "=".repeat(4 - (b64.length % 4));
    const payload = JSON.parse(atob(b64 + pad)) as Record<string, unknown>;
    if (payload.email_confirmed === true) return true;
    if (payload.email_confirmed === false) return false;
    return null;
  } catch {
    return null;
  }
}
