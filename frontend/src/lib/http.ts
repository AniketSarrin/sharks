import { getAccessToken } from "./auth-storage";
import { apiBaseUrl } from "./config";

function resolveUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${apiBaseUrl}${normalizedPath}`;
}

function normalizeHeaders(init?: RequestInit): Headers {
  const headers = new Headers(init?.headers);
  const token = getAccessToken();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const body = init?.body;
  if (body !== undefined && typeof body === "string" && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  return headers;
}

/** Gateway-relative fetch with optional Bearer token and JSON Content-Type for string bodies. */
export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const url = resolveUrl(path);
  const headers = normalizeHeaders(init);
  return fetch(url, { ...init, headers });
}

function pickMessage(parsed: Record<string, unknown>): string | undefined {
  if (typeof parsed.detail === "string") return parsed.detail;
  if (typeof parsed.title === "string") return parsed.title;
  if (typeof parsed.message === "string") return parsed.message;
  if (typeof parsed.error === "string") return parsed.error;
  const errors = parsed.errors;
  if (Array.isArray(errors) && errors.length > 0) {
    const first = errors[0];
    if (first != null && typeof first === "object" && typeof (first as { defaultMessage?: string }).defaultMessage === "string") {
      return String((first as { defaultMessage: string }).defaultMessage);
    }
  }
  return undefined;
}

/** Best-effort message from Spring / JSON error payloads. */
export async function extractApiErrorMessage(response: Response): Promise<string> {
  const fallback = `${response.status} ${response.statusText || "Request failed"}`.trim();
  const ct = response.headers.get("Content-Type") ?? "";
  if (!ct.includes("json")) {
    const text = await response.text().catch(() => "");
    return text.trim() || fallback;
  }
  try {
    const parsed = (await response.json()) as Record<string, unknown>;
    return pickMessage(parsed) ?? fallback;
  } catch {
    return fallback;
  }
}
