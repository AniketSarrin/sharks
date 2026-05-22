/** API gateway base URL (no trailing slash). Override with VITE_API_BASE_URL for non-local gateways. */
export const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");
