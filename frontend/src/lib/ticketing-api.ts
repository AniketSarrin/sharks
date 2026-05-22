import type { Ticket, TicketReceiptResponse } from "./api-types";
import { eventsApi } from "./events-api";
import { apiFetch, extractApiErrorMessage } from "./http";

// ─── Enriched Ticket ──────────────────────────────────────────────────────────

/**
 * Converts a raw TicketReceiptResponse into the legacy Ticket shape used by
 * MyTickets.tsx and other UI components, enriched with event details fetched
 * from the events service via POST /api/events/by-ids.
 */
async function enrichTickets(receipts: TicketReceiptResponse[]): Promise<Ticket[]> {
  if (receipts.length === 0) return [];

  const uniqueEventIds = [...new Set(receipts.map((r) => r.eventId))];
  const eventsResult = await eventsApi.getByIds(uniqueEventIds);

  const eventMap = new Map<number, { name: string; eventTime: string; address: string }>();
  if (eventsResult.success) {
    for (const e of eventsResult.data) {
      eventMap.set(e.id, { name: e.name, eventTime: e.eventTime, address: e.address });
    }
  }

  return receipts.map((r): Ticket => {
    const ev = eventMap.get(r.eventId);
    let eventDate = "";
    let eventTime = "";
    if (ev) {
      const dt = new Date(ev.eventTime);
      eventDate = dt.toISOString().slice(0, 10);
      eventTime = dt.toTimeString().slice(0, 5);
    }

    return {
      id: String(r.id),
      eventId: String(r.eventId),
      eventTitle: ev?.name ?? `Event #${r.eventId}`,
      eventDate,
      eventTime,
      eventLocation: ev?.address ?? "",
      userId: "",
      userName: "",
      quantity: r.quantity,
      totalPrice: r.totalPrice,
      status: r.status,
      purchasedAt: r.purchasedAt.slice(0, 10),
      ticketCode: r.ticketCode,
    };
  });
}

// ─── Ticketing API ────────────────────────────────────────────────────────────

export const ticketingApi = {
  async purchase(
    eventId: number,
    quantity: number
  ): Promise<
    { success: true; data: TicketReceiptResponse } | { success: false; error: string }
  > {
    const res = await apiFetch("/api/tickets/purchase", {
      method: "POST",
      body: JSON.stringify({ eventId, quantity }),
    });
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const receipt = (await res.json()) as TicketReceiptResponse;
    return { success: true, data: receipt };
  },

  async getMyTickets(): Promise<
    { success: true; data: Ticket[] } | { success: false; error: string }
  > {
    const res = await apiFetch("/api/tickets/my-tickets");
    if (!res.ok) {
      const msg = await extractApiErrorMessage(res);
      return { success: false, error: msg };
    }
    const receipts = (await res.json()) as TicketReceiptResponse[];
    const enriched = await enrichTickets(receipts);
    return { success: true, data: enriched };
  },
};
