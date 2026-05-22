import type {
  BackendEvent,
  BackendPage,
  BackendEventType,
  CreateEventRequest,
} from "./api-types";
import type { Event } from "./mock-data";
import { apiFetch, extractApiErrorMessage } from "./http";

// ─── BackendEvent → frontend Event mapper ─────────────────────────────────────

const TYPE_TO_CATEGORY: Record<BackendEventType, string> = {
  MUSIC: "Music",
  NETWORKING: "Networking",
  DATING: "Dating",
};

const PLACEHOLDER_IMAGE =
  "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop";

export function mapBackendEvent(e: BackendEvent): Event {
  const dt = new Date(e.eventTime);
  const date = dt.toISOString().slice(0, 10);
  const time = dt.toTimeString().slice(0, 5);
  const raw = e as BackendEvent & Record<string, unknown>;
  const priceCandidates = [
    raw.price,
    raw.unitPrice,
    raw.ticketPrice,
    raw.ticket_price,
    raw.minPrice,
  ];
  const normalizedPrice =
    priceCandidates
      .map((value) => {
        if (typeof value === "number" && Number.isFinite(value)) return value;
        if (typeof value === "string") {
          const parsed = Number.parseFloat(value);
          return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
      })
      .find((value): value is number => value != null) ?? 0;

  const addressParts = e.address.split(",").map((s) => s.trim());
  const city = addressParts.length > 1 ? addressParts[addressParts.length - 1] : "";
  const location = addressParts.length > 1 ? addressParts.slice(0, -1).join(", ") : e.address;

  return {
    id: String(e.id),
    title: e.name,
    description: e.description ?? "",
    date,
    time,
    endTime: "",
    location,
    address: e.address,
    city,
    category: e.type ? TYPE_TO_CATEGORY[e.type] : "Community",
    capacity: e.ticketsProvisioned,
    attendees: 0,
    price: normalizedPrice,
    image: e.imageUrl ?? PLACEHOLDER_IMAGE,
    organizerId: e.organizerId,
    organizer: {
      name: "",
      avatar: `https://ui-avatars.com/api/?name=O&background=e8553a&color=fff`,
      bio: "",
    },
    tags: e.singer ? [e.singer] : [],
    isFeatured: false,
    status: dt > new Date() ? "upcoming" : "past",
  };
}

// ─── API filters ──────────────────────────────────────────────────────────────

export interface EventFilters {
  name?: string;
  location?: string;
  date?: string;
  category?: string;
  page?: number;
  size?: number;
}

// ─── Events API ───────────────────────────────────────────────────────────────

export const eventsApi = {
  async getAll(
    filters?: EventFilters
  ): Promise<{ success: true; data: Event[]; total: number } | { success: false; error: string }> {
    const params = new URLSearchParams();
    if (filters?.name) params.set("name", filters.name);
    if (filters?.location) params.set("location", filters.location);
    if (filters?.date) params.set("date", filters.date);
    if (filters?.category) params.set("category", filters.category);
    if (filters?.page != null) params.set("page", String(filters.page));
    if (filters?.size != null) params.set("size", String(filters.size));

    const qs = params.toString();
    const res = await apiFetch(`/api/events${qs ? `?${qs}` : ""}`);
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const page = (await res.json()) as BackendPage<BackendEvent>;
    return {
      success: true,
      data: page.content.map(mapBackendEvent),
      total: page.totalElements,
    };
  },

  async getById(
    id: string | number
  ): Promise<{ success: true; data: Event } | { success: false; error: string }> {
    const res = await apiFetch(`/api/events/${id}`);
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const event = (await res.json()) as BackendEvent;
    return { success: true, data: mapBackendEvent(event) };
  },

  async getByIds(
    ids: number[]
  ): Promise<{ success: true; data: BackendEvent[] } | { success: false; error: string }> {
    if (ids.length === 0) return { success: true, data: [] };
    const res = await apiFetch("/api/events/by-ids", {
      method: "POST",
      body: JSON.stringify({ ids }),
    });
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const events = (await res.json()) as BackendEvent[];
    return { success: true, data: events };
  },

  async create(
    data: CreateEventRequest,
    image?: File
  ): Promise<{ success: true; data: Event } | { success: false; error: string }> {
    const formData = new FormData();
    formData.append("event", new Blob([JSON.stringify(data)], { type: "application/json" }));
    if (image) formData.append("image", image);

    const res = await apiFetch("/api/events", {
      method: "POST",
      body: formData,
    });
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const event = (await res.json()) as BackendEvent;
    return { success: true, data: mapBackendEvent(event) };
  },

  async update(
    id: string | number,
    data: Partial<CreateEventRequest>
  ): Promise<{ success: true; data: Event } | { success: false; error: string }> {
    const res = await apiFetch(`/api/events/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const event = (await res.json()) as BackendEvent;
    return { success: true, data: mapBackendEvent(event) };
  },

  async delete(
    id: string | number
  ): Promise<{ success: true } | { success: false; error: string }> {
    const res = await apiFetch(`/api/events/${id}`, { method: "DELETE" });
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    return { success: true };
  },
};
