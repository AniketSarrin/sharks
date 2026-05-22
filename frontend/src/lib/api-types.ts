export interface User {
  id: string;
  name: string;
  email: string;
  role: "attendee" | "organizer" | "admin";
  avatar: string;
  createdAt: string;
  emailVerified: boolean;
}

/** Value from user service `UserRole` / `UserDto.role` (JSON). */
export type BackendUserRole = "attendee" | "organizer" | "admin";

/** `GET /api/v1/users/me` — base user row (matches Java `UserDto`). */
export interface UserMeDto {
  id: string;
  email: string;
  fullName: string;
  avatarUrl: string | null;
  role: BackendUserRole;
  bio: string | null;
  phone: string | null;
  location: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

/** `GET /api/v1/{attendees|organizers|admins}/me` — role profile + extensions. */
export interface RoleProfileDto extends UserMeDto {
  nickname?: string | null;
  organizerDescription?: string | null;
}

export interface BaseProfilePatchBody {
  fullName?: string;
  avatarUrl?: string;
  bio?: string;
  phone?: string;
  location?: string;
}

export interface AttendeeProfilePatchBody extends BaseProfilePatchBody {
  nickname?: string;
}

export interface OrganizerProfilePatchBody extends BaseProfilePatchBody {
  organizerDescription?: string;
}

export type AdminProfilePatchBody = BaseProfilePatchBody;

export type RoleProfilePatchBody =
  | AttendeeProfilePatchBody
  | OrganizerProfilePatchBody
  | AdminProfilePatchBody;

export function roleProfilePathSegment(role: BackendUserRole): "attendees" | "organizers" | "admins" {
  switch (role) {
    case "attendee":
      return "attendees";
    case "organizer":
      return "organizers";
    case "admin":
      return "admins";
  }
}

/** Normalize role strings from the API before use as `BackendUserRole`. */
export function normalizeBackendUserRole(raw: string): BackendUserRole | null {
  const r = raw?.trim().toLowerCase();
  if (r === "attendee" || r === "organizer" || r === "admin") return r;
  return null;
}

export interface Ticket {
  id: string;
  eventId: string;
  eventTitle: string;
  eventDate: string;
  eventTime: string;
  eventLocation: string;
  userId: string;
  userName: string;
  quantity: number;
  totalPrice: number;
  status: "confirmed" | "pending" | "refunded" | "cancelled";
  purchasedAt: string;
  ticketCode: string;
}

export interface Organization {
  id: string;
  name: string;
  description: string;
  logo: string;
  website: string;
  eventsCount: number;
  membersCount: number;
  createdAt: string;
  ownerId: string;
}

export interface DashboardStats {
  totalUsers: number;
  totalEvents: number;
  totalTickets: number;
  totalRevenue: number;
  recentSignups: number;
  activeEvents: number;
  ticketsSoldToday: number;
  pendingApprovals: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: string;
}

/** GoTrue user object returned inside the login response. */
export interface GoTrueUser {
  id?: string;
  email?: string;
  app_metadata?: {
    role?: string;
    [key: string]: unknown;
  };
  user_metadata?: Record<string, unknown>;
  [key: string]: unknown;
}

/** JSON from POST /api/v1/auth/login (backend LoginResponse; user is GoTrue payload). */
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  expiresAt: number;
  refreshToken: string;
  user: GoTrueUser;
}

// ─── Backend API shapes ────────────────────────────────────────────────────────

/** Matches Java EventType enum. */
export type BackendEventType = "MUSIC" | "NETWORKING" | "DATING";

/** Matches Java EventResponse record from the events service. */
export interface BackendEvent {
  id: number;
  name: string;
  address: string;
  eventTime: string;
  ticketsProvisioned: number;
  price?: number | string | null;
  description: string | null;
  type: BackendEventType | null;
  organizerId: string;
  singer: string | null;
  minAge: number | null;
  maxAge: number | null;
  imageUrl: string | null;
}

/** Spring Data Page wrapper around any content type. */
export interface BackendPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/** Matches Java TicketReceiptResponse record from the ticketing service. */
export interface TicketReceiptResponse {
  id: number;
  eventId: number;
  ticketId: number | null;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  ticketCode: string;
  purchasedAt: string;
  status: "confirmed" | "pending" | "refunded" | "cancelled";
}

/** POST /api/v1/users/register request body (matches Java RegisterRequest). */
export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role?: string;
}

/** POST /api/events request body (matches Java CreateEventRequest). */
export interface CreateEventRequest {
  name: string;
  address: string;
  eventTime: string;
  ticketsProvisioned: number;
  price: number;
  description?: string;
  type?: BackendEventType;
  singer?: string;
  minAge?: number;
  maxAge?: number;
}
