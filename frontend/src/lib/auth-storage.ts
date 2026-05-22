import type { BackendUserRole } from "./api-types";
import { normalizeBackendUserRole } from "./api-types";

const ACCESS_KEY = "sharks_access_token";
const REFRESH_KEY = "sharks_refresh_token";
const EXPIRES_AT_KEY = "sharks_token_expires_at";
const ROLE_KEY = "sharks_user_role";

export interface StoredSession {
  accessToken: string;
  refreshToken: string;
  expiresAt?: number | null;
  role?: BackendUserRole | null;
}

/** Persists JWT and related session fields for authenticated API calls. Prefer httpOnly cookies in production hardening. */
export function setSession(session: StoredSession): void {
  localStorage.setItem(ACCESS_KEY, session.accessToken);
  localStorage.setItem(REFRESH_KEY, session.refreshToken);
  if (session.expiresAt != null) {
    localStorage.setItem(EXPIRES_AT_KEY, String(session.expiresAt));
  } else {
    localStorage.removeItem(EXPIRES_AT_KEY);
  }
  if (session.role != null) {
    localStorage.setItem(ROLE_KEY, session.role);
  } else {
    localStorage.removeItem(ROLE_KEY);
  }
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY);
}

/** Returns the role stored at login time from GoTrue app_metadata, or null if absent/invalid. */
export function getStoredRole(): BackendUserRole | null {
  const raw = localStorage.getItem(ROLE_KEY);
  if (!raw) return null;
  return normalizeBackendUserRole(raw);
}

export function clearSession(): void {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(EXPIRES_AT_KEY);
  localStorage.removeItem(ROLE_KEY);
}
