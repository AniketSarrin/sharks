import type { User, Ticket, Organization, DashboardStats, LoginResponse } from "./api-types";
import { normalizeBackendUserRole } from "./api-types";
import type { UserMeDto } from "./api-types";
import { clearSession, setSession } from "./auth-storage";
import { apiFetch, extractApiErrorMessage } from "./http";
import { mockEvents } from "./mock-data";

// ── Mock Users ──
export const mockUsers: User[] = [
  { id: "u1", name: "Alice Johnson", email: "alice@example.com", role: "admin", avatar: "https://ui-avatars.com/api/?name=AJ&background=e8553a&color=fff", createdAt: "2025-12-01", emailVerified: true },
  { id: "u2", name: "Bob Martinez", email: "bob@example.com", role: "organizer", avatar: "https://ui-avatars.com/api/?name=BM&background=7c3aed&color=fff", createdAt: "2026-01-15", emailVerified: true },
  { id: "u3", name: "Carol Chen", email: "carol@example.com", role: "attendee", avatar: "https://ui-avatars.com/api/?name=CC&background=059669&color=fff", createdAt: "2026-02-10", emailVerified: true },
  { id: "u4", name: "David Kim", email: "david@example.com", role: "attendee", avatar: "https://ui-avatars.com/api/?name=DK&background=0891b2&color=fff", createdAt: "2026-02-28", emailVerified: true },
  { id: "u5", name: "Eva Rodriguez", email: "eva@example.com", role: "organizer", avatar: "https://ui-avatars.com/api/?name=ER&background=db2777&color=fff", createdAt: "2026-03-05", emailVerified: true },
  { id: "u6", name: "Frank Lee", email: "frank@example.com", role: "attendee", avatar: "https://ui-avatars.com/api/?name=FL&background=ea580c&color=fff", createdAt: "2026-03-20", emailVerified: false },
];

// ── Mock Tickets ──
export const mockTickets: Ticket[] = [
  { id: "t1", eventId: "1", eventTitle: "Summer Music Festival 2026", eventDate: "2026-07-15", eventTime: "14:00", eventLocation: "Golden Gate Park, San Francisco, CA", userId: "u3", userName: "Carol Chen", quantity: 2, totalPrice: 0, status: "confirmed", purchasedAt: "2026-04-01", ticketCode: "SHK-MF2026-001" },
  { id: "t2", eventId: "2", eventTitle: "Tech Startup Pitch Night", eventDate: "2026-05-20", eventTime: "18:00", eventLocation: "Innovation Hub, San Jose, CA", userId: "u3", userName: "Carol Chen", quantity: 1, totalPrice: 15, status: "confirmed", purchasedAt: "2026-04-05", ticketCode: "SHK-PN2026-002" },
  { id: "t3", eventId: "5", eventTitle: "AI & Machine Learning Conference", eventDate: "2026-08-10", eventTime: "09:00", eventLocation: "Moscone Center, San Francisco, CA", userId: "u4", userName: "David Kim", quantity: 1, totalPrice: 299, status: "pending", purchasedAt: "2026-04-08", ticketCode: "SHK-AI2026-003" },
  { id: "t4", eventId: "3", eventTitle: "Farm-to-Table Dinner Experience", eventDate: "2026-06-08", eventTime: "19:00", eventLocation: "The Garden Table, Napa, CA", userId: "u6", userName: "Frank Lee", quantity: 2, totalPrice: 170, status: "confirmed", purchasedAt: "2026-04-02", ticketCode: "SHK-FT2026-004" },
  { id: "t5", eventId: "4", eventTitle: "Community Yoga in the Park", eventDate: "2026-05-25", eventTime: "08:00", eventLocation: "Marina Green, San Francisco, CA", userId: "u3", userName: "Carol Chen", quantity: 1, totalPrice: 0, status: "refunded", purchasedAt: "2026-03-28", ticketCode: "SHK-YG2026-005" },
  { id: "t6", eventId: "7", eventTitle: "Charity 5K Run for Education", eventDate: "2026-06-15", eventTime: "07:00", eventLocation: "Lakeside Park, Oakland, CA", userId: "u4", userName: "David Kim", quantity: 1, totalPrice: 30, status: "confirmed", purchasedAt: "2026-04-10", ticketCode: "SHK-5K2026-006" },
];

// ── Mock Organizations ──
export const mockOrganizations: Organization[] = [
  { id: "o1", name: "Bay Area Events Co.", description: "Premier event organizers in the Bay Area since 2015. We specialize in large-scale outdoor festivals and community gatherings.", logo: "https://ui-avatars.com/api/?name=BA&background=e8553a&color=fff&size=128", website: "https://bayareaevents.example.com", eventsCount: 24, membersCount: 8, createdAt: "2025-06-15", ownerId: "u2" },
  { id: "o2", name: "StartupSJ", description: "Connecting the startup ecosystem in Silicon Valley through networking events, pitch nights, and workshops.", logo: "https://ui-avatars.com/api/?name=SJ&background=7c3aed&color=fff&size=128", website: "https://startupsj.example.com", eventsCount: 12, membersCount: 4, createdAt: "2025-09-01", ownerId: "u2" },
  { id: "o3", name: "TechForward", description: "Leading technology conferences worldwide. Bringing together innovators, researchers, and industry leaders.", logo: "https://ui-avatars.com/api/?name=TF&background=4f46e5&color=fff&size=128", website: "https://techforward.example.com", eventsCount: 6, membersCount: 15, createdAt: "2024-03-20", ownerId: "u5" },
  { id: "o4", name: "Napa Culinary Arts", description: "Celebrating culinary excellence in wine country with farm-to-table experiences and cooking classes.", logo: "https://ui-avatars.com/api/?name=NC&background=059669&color=fff&size=128", website: "https://napaculinary.example.com", eventsCount: 18, membersCount: 6, createdAt: "2025-01-10", ownerId: "u5" },
];

// ── Mock Dashboard Stats ──
export const mockDashboardStats: DashboardStats = {
  totalUsers: 1284,
  totalEvents: 156,
  totalTickets: 8432,
  totalRevenue: 245680,
  recentSignups: 47,
  activeEvents: 32,
  ticketsSoldToday: 89,
  pendingApprovals: 5,
};

// ── Simulated API delay ──
const delay = (ms = 300) => new Promise((r) => setTimeout(r, ms));

// Current mock user (simulates logged-in user)
let currentUser: User = { ...mockUsers[2] }; // Carol Chen (attendee)

// ══════════════════════════════════════════
// AUTH API
// POST /api/v1/users/register
// POST /api/v1/auth/login
// POST /api/v1/auth/logout
// POST /api/v1/auth/refresh-token
// POST /api/v1/auth/forgot-password
// POST /api/v1/auth/reset-password
// POST /api/v1/auth/verify-email
// POST /api/v1/auth/resend-verification
// GET  /api/auth/me
// ══════════════════════════════════════════
export const authApi = {
  register: async (data: { name: string; email: string; password: string; role: string }) => {
    const res = await apiFetch("/api/v1/users/register", {
      method: "POST",
      body: JSON.stringify({ fullName: data.name, email: data.email, password: data.password, role: data.role }),
    });
    if (!res.ok) {
      const message = await extractApiErrorMessage(res);
      return { success: false as const, error: message };
    }
    const userDto = (await res.json()) as UserMeDto;
    const newUser: User = {
      id: userDto.id,
      name: userDto.fullName,
      email: userDto.email,
      role: userDto.role,
      avatar: userDto.avatarUrl ?? `https://ui-avatars.com/api/?name=${encodeURIComponent(userDto.fullName)}&background=e8553a&color=fff`,
      createdAt: userDto.createdAt.slice(0, 10),
      emailVerified: false,
    };
    return { success: true as const, data: { user: newUser }, message: "Registration successful. Please verify your email." };
  },
  login: async (data: { email: string; password: string }) => {
    const res = await apiFetch("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify({ email: data.email, password: data.password }),
    });
    if (!res.ok) {
      const message = await extractApiErrorMessage(res);
      return { success: false as const, error: message };
    }
    const body = (await res.json()) as LoginResponse;
    const roleRaw = body.user?.app_metadata?.role;
    const role = typeof roleRaw === "string" ? normalizeBackendUserRole(roleRaw) : null;
    setSession({
      accessToken: body.accessToken,
      refreshToken: body.refreshToken,
      expiresAt: body.expiresAt ?? null,
      role,
    });
    return {
      success: true as const,
      data: {
        token: body.accessToken,
        refreshToken: body.refreshToken,
        role,
        user: body.user,
      },
    };
  },
  logout: async () => {
    clearSession();
    return { success: true, message: "Logged out successfully" };
  },
  refreshToken: async (data: { refreshToken: string }) => {
    await delay();
    return { success: true, data: { token: "mock-new-jwt-token", refreshToken: "mock-new-refresh-token" } };
  },
  forgotPassword: async (data: { email: string }) => {
    await delay();
    return { success: true, message: "Password reset link sent to your email" };
  },
  resetPassword: async (data: { token: string; newPassword: string }) => {
    await delay();
    return { success: true, message: "Password reset successfully" };
  },
  verifyEmail: async (data: { token: string }) => {
    await delay();
    currentUser.emailVerified = true;
    return { success: true, message: "Email verified successfully" };
  },
  resendVerification: async (data: { email: string }) => {
    await delay();
    return { success: true, message: "Verification email sent" };
  },
  me: async () => {
    await delay();
    return { success: true, data: currentUser };
  },
};

// ══════════════════════════════════════════
// USER API
// GET    /api/v1/users/me
// PUT    /api/v1/users/me
// PATCH  /api/v1/users/me
// DELETE /api/v1/users/me
// GET    /api/v1/users/me/role
// POST   /api/v1/users/{userId}/role   (admin)
// PATCH  /api/v1/users/{userId}/role   (admin)
// GET    /api/v1/users/me/events-created
// ══════════════════════════════════════════
export const usersApi = {
  getMe: async () => {
    await delay();
    return { success: true, data: currentUser };
  },
  updateMe: async (data: Partial<User>) => {
    await delay();
    currentUser = { ...currentUser, ...data };
    return { success: true, data: currentUser, message: "Profile updated" };
  },
  patchMe: async (data: Partial<User>) => {
    await delay();
    currentUser = { ...currentUser, ...data };
    return { success: true, data: currentUser, message: "Profile patched" };
  },
  deleteMe: async () => {
    await delay();
    return { success: true, message: "Account deleted" };
  },
  getMyRole: async () => {
    await delay();
    return { success: true, data: { role: currentUser.role } };
  },
  assignRole: async (userId: string, role: User["role"]) => {
    await delay();
    const user = mockUsers.find((u) => u.id === userId);
    if (!user) return { success: false, error: "User not found" };
    return { success: true, data: { ...user, role }, message: `Role updated to ${role}` };
  },
  patchRole: async (userId: string, role: User["role"]) => {
    await delay();
    const user = mockUsers.find((u) => u.id === userId);
    if (!user) return { success: false, error: "User not found" };
    return { success: true, data: { ...user, role }, message: `Role patched to ${role}` };
  },
  getMyEventsCreated: async () => {
    await delay();
    // Simulate organizer's created events
    return { success: true, data: mockEvents.slice(0, 3) };
  },
  getMyTicketsHistory: async () => {
    await delay();
    return { success: true, data: mockTickets.filter((t) => t.userId === currentUser.id) };
  },
};

// ══════════════════════════════════════════
// CUSTOMER API
// GET /api/v1/users/me/events-attending
// GET /api/v1/users/me/events/history
// ══════════════════════════════════════════
export const customerApi = {
  getEventsAttending: async () => {
    await delay();
    const myTickets = mockTickets.filter((t) => t.userId === currentUser.id && t.status === "confirmed");
    const eventIds = myTickets.map((t) => t.eventId);
    const events = mockEvents.filter((e) => eventIds.includes(e.id));
    return { success: true, data: events };
  },
  getEventsHistory: async () => {
    await delay();
    const myTickets = mockTickets.filter((t) => t.userId === currentUser.id);
    const eventIds = myTickets.map((t) => t.eventId);
    const events = mockEvents.filter((e) => eventIds.includes(e.id));
    return { success: true, data: events.map((e) => ({ ...e, ticketStatus: myTickets.find((t) => t.eventId === e.id)?.status })) };
  },
};

// ══════════════════════════════════════════
// EVENTS API
// GET    /api/events?location=&date=&category=
// POST   /api/events
// GET    /api/events/:id
// PUT    /api/events/:id
// DELETE /api/events/:id
// ══════════════════════════════════════════
export const eventsApi = {
  getAll: async (filters?: { category?: string; search?: string; location?: string; date?: string }) => {
    await delay();
    let events = [...mockEvents];
    if (filters?.category) events = events.filter((e) => e.category === filters.category);
    if (filters?.location) {
      const loc = filters.location.toLowerCase();
      events = events.filter((e) => e.city.toLowerCase().includes(loc) || e.location.toLowerCase().includes(loc));
    }
    if (filters?.date) events = events.filter((e) => e.date === filters.date);
    if (filters?.search) {
      const q = filters.search.toLowerCase();
      events = events.filter((e) => e.title.toLowerCase().includes(q) || e.city.toLowerCase().includes(q));
    }
    return { success: true, data: events };
  },
  getById: async (id: string) => {
    await delay();
    const event = mockEvents.find((e) => e.id === id);
    if (!event) return { success: false, error: "Event not found" };
    return { success: true, data: event };
  },
  create: async (data: Partial<typeof mockEvents[0]>) => {
    await delay();
    return { success: true, data: { ...data, id: `e${Date.now()}`, status: "pending" }, message: "Event created and pending approval" };
  },
  update: async (id: string, data: Partial<typeof mockEvents[0]>) => {
    await delay();
    return { success: true, data: { ...data, id }, message: "Event updated" };
  },
  delete: async (id: string) => {
    await delay();
    return { success: true, message: "Event deleted" };
  },
};

// ══════════════════════════════════════════
// TICKETS API
// POST /api/tickets/purchase
// GET  /api/tickets/my-tickets
// PUT  /api/tickets/:id/refund
// ══════════════════════════════════════════
export const ticketsApi = {
  purchase: async (data: { eventId: string; quantity: number }) => {
    await delay();
    const event = mockEvents.find((e) => e.id === data.eventId);
    if (!event) return { success: false, error: "Event not found" };
    const ticket: Ticket = {
      id: `t${Date.now()}`, eventId: data.eventId, eventTitle: event.title, eventDate: event.date, eventTime: event.time, eventLocation: `${event.location}, ${event.city}`, userId: currentUser.id, userName: currentUser.name, quantity: data.quantity, totalPrice: event.price * data.quantity, status: "confirmed", purchasedAt: new Date().toISOString().split("T")[0], ticketCode: `SHK-${Date.now().toString(36).toUpperCase()}`,
    };
    return { success: true, data: ticket, message: "Ticket purchased successfully" };
  },
  getMyTickets: async () => {
    await delay();
    return { success: true, data: mockTickets.filter((t) => t.userId === currentUser.id) };
  },
  refund: async (id: string) => {
    await delay();
    const ticket = mockTickets.find((t) => t.id === id);
    if (!ticket) return { success: false, error: "Ticket not found" };
    return { success: true, data: { ...ticket, status: "refunded" }, message: "Refund issued" };
  },
};

// ══════════════════════════════════════════
// ORGANIZATIONS API
// GET  /api/organizations
// POST /api/organizations
// GET  /api/organizations/:id
// ══════════════════════════════════════════
export const organizationsApi = {
  getAll: async () => {
    await delay();
    return { success: true, data: mockOrganizations };
  },
  getById: async (id: string) => {
    await delay();
    const org = mockOrganizations.find((o) => o.id === id);
    if (!org) return { success: false, error: "Organization not found" };
    return { success: true, data: org };
  },
  create: async (data: Partial<Organization>) => {
    await delay();
    return { success: true, data: { ...data, id: `o${Date.now()}` }, message: "Organization created" };
  },
};

// ══════════════════════════════════════════
// ADMIN API
// GET  /api/admin/dashboard
// GET  /api/admin/users
// GET  /api/admin/events
// GET  /api/admin/user_ticket/:user_id,:ticket_id
// POST /api/admin/user_ticket/:user_id,:ticket_id
// ══════════════════════════════════════════
export const adminApi = {
  getDashboard: async () => {
    await delay();
    return { success: true, data: mockDashboardStats };
  },
  getUsers: async () => {
    await delay();
    return { success: true, data: mockUsers };
  },
  getEvents: async () => {
    await delay();
    return { success: true, data: mockEvents };
  },
  getUserTicket: async (userId: string, ticketId: string) => {
    await delay();
    const ticket = mockTickets.find((t) => t.id === ticketId && t.userId === userId);
    if (!ticket) return { success: false, error: "User ticket not found" };
    const user = mockUsers.find((u) => u.id === userId);
    return { success: true, data: { ticket, user } };
  },
  createUserTicket: async (userId: string, data: { eventId: string; quantity: number }) => {
    await delay();
    const event = mockEvents.find((e) => e.id === data.eventId);
    const user = mockUsers.find((u) => u.id === userId);
    if (!event || !user) return { success: false, error: "Event or user not found" };
    const ticket: Ticket = {
      id: `t${Date.now()}`, eventId: data.eventId, eventTitle: event.title, eventDate: event.date, eventTime: event.time, eventLocation: `${event.location}, ${event.city}`, userId, userName: user.name, quantity: data.quantity, totalPrice: event.price * data.quantity, status: "confirmed", purchasedAt: new Date().toISOString().split("T")[0], ticketCode: `SHK-ADM-${Date.now().toString(36).toUpperCase()}`,
    };
    return { success: true, data: ticket, message: "Ticket assigned to user" };
  },
};
