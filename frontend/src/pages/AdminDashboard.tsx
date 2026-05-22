import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { adminApi, usersApi, mockUsers, mockTickets } from "@/lib/api";
import { mockEvents } from "@/lib/mock-data";
import type { DashboardStats, User, Ticket } from "@/lib/api-types";
import type { Event } from "@/lib/mock-data";
import { Users, Calendar, TicketIcon, DollarSign, TrendingUp, ShieldCheck, UserCheck, Clock } from "lucide-react";
import { toast } from "sonner";

const statCards = (stats: DashboardStats) => [
  { label: "Total Users", value: stats.totalUsers.toLocaleString(), icon: Users, color: "text-primary" },
  { label: "Total Events", value: stats.totalEvents.toLocaleString(), icon: Calendar, color: "text-accent" },
  { label: "Tickets Sold", value: stats.totalTickets.toLocaleString(), icon: TicketIcon, color: "text-success" },
  { label: "Revenue", value: `$${stats.totalRevenue.toLocaleString()}`, icon: DollarSign, color: "text-warning" },
  { label: "Signups (7d)", value: stats.recentSignups.toString(), icon: UserCheck, color: "text-primary" },
  { label: "Active Events", value: stats.activeEvents.toString(), icon: TrendingUp, color: "text-success" },
  { label: "Sold Today", value: stats.ticketsSoldToday.toString(), icon: TicketIcon, color: "text-accent" },
  { label: "Pending Approval", value: stats.pendingApprovals.toString(), icon: Clock, color: "text-warning" },
];

const roleColors: Record<string, string> = {
  admin: "bg-destructive/10 text-destructive border-0",
  organizer: "bg-accent/10 text-accent border-0",
  attendee: "bg-primary/10 text-primary border-0",
};

const statusColors: Record<string, string> = {
  confirmed: "bg-success/10 text-success border-0",
  pending: "bg-warning/10 text-warning border-0",
  refunded: "bg-muted text-muted-foreground border-0",
  cancelled: "bg-destructive/10 text-destructive border-0",
};

const AdminDashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [events, setEvents] = useState<Event[]>([]);
  const [tickets] = useState<Ticket[]>(mockTickets);
  const [tab, setTab] = useState<"overview" | "users" | "events" | "tickets">("overview");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([adminApi.getDashboard(), adminApi.getUsers(), adminApi.getEvents()]).then(
      ([dashRes, usersRes, eventsRes]) => {
        if (dashRes.success) setStats(dashRes.data);
        if (usersRes.success) setUsers(usersRes.data);
        if (eventsRes.success) setEvents(eventsRes.data);
        setLoading(false);
      },
    );
  }, []);

  const handleRoleChange = async (userId: string, newRole: string) => {
    const res = await usersApi.assignRole(userId, newRole as User["role"]);
    if (res.success) {
      setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role: newRole as User["role"] } : u)));
      toast.success(res.message);
    }
  };

  const tabs = [
    { key: "overview" as const, label: "Overview", endpoint: "GET /api/admin/dashboard" },
    { key: "users" as const, label: "Users", endpoint: "GET /api/admin/users" },
    { key: "events" as const, label: "Events", endpoint: "GET /api/admin/events" },
    { key: "tickets" as const, label: "User Tickets", endpoint: "GET /api/admin/user_ticket/:user_id,:ticket_id" },
  ];

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-12">
        <div className="mb-8">
          <div className="flex items-center gap-2 mb-1">
            <ShieldCheck className="h-6 w-6 text-primary" />
            <h1 className="text-3xl font-extrabold text-foreground">Admin Dashboard</h1>
          </div>
          <p className="text-muted-foreground">Manage users, events, and tickets</p>
        </div>

        {/* Tabs */}
        <div className="mb-6 flex gap-1 overflow-x-auto rounded-lg bg-secondary p-1">
          {tabs.map((t) => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`shrink-0 rounded-md px-4 py-2 text-sm font-medium transition-colors ${
                tab === t.key ? "bg-card text-card-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <p className="mb-6 text-xs text-muted-foreground">
          <code className="rounded bg-muted px-1.5 py-0.5">{tabs.find((t) => t.key === tab)?.endpoint}</code>
        </p>

        {loading ? (
          <div className="py-20 text-center text-muted-foreground">Loading...</div>
        ) : (
          <>
            {tab === "overview" && stats && (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                {statCards(stats).map((s) => (
                  <Card key={s.label} className="card-shadow">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                      <CardTitle className="text-sm font-medium text-muted-foreground">{s.label}</CardTitle>
                      <s.icon className={`h-5 w-5 ${s.color}`} />
                    </CardHeader>
                    <CardContent>
                      <p className="text-2xl font-extrabold text-card-foreground">{s.value}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}

            {tab === "users" && (
              <div className="space-y-4">
                <div className="flex gap-2 flex-wrap text-xs text-muted-foreground">
                  <code className="rounded bg-muted px-1.5 py-0.5">POST /api/v1/users/{"{userId}"}/role</code>
                  <code className="rounded bg-muted px-1.5 py-0.5">PATCH /api/v1/users/{"{userId}"}/role</code>
                </div>
                <div className="rounded-xl border border-border bg-card card-shadow overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-border bg-muted/50">
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">User</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Email</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Role</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Verified</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Joined</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Assign Role</th>
                        </tr>
                      </thead>
                      <tbody>
                        {users.map((u) => (
                          <tr
                            key={u.id}
                            className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors"
                          >
                            <td className="px-4 py-3">
                              <div className="flex items-center gap-3">
                                <img src={u.avatar} alt={u.name} className="h-8 w-8 rounded-full" />
                                <span className="font-medium text-card-foreground">{u.name}</span>
                              </div>
                            </td>
                            <td className="px-4 py-3 text-muted-foreground">{u.email}</td>
                            <td className="px-4 py-3">
                              <Badge className={roleColors[u.role]}>{u.role}</Badge>
                            </td>
                            <td className="px-4 py-3">
                              <Badge
                                className={
                                  u.emailVerified
                                    ? "bg-success/10 text-success border-0"
                                    : "bg-warning/10 text-warning border-0"
                                }
                              >
                                {u.emailVerified ? "Yes" : "No"}
                              </Badge>
                            </td>
                            <td className="px-4 py-3 text-muted-foreground">{u.createdAt}</td>
                            <td className="px-4 py-3">
                              <Select value={u.role} onValueChange={(v) => handleRoleChange(u.id, v)}>
                                <SelectTrigger className="h-8 w-[120px]">
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="attendee">Attendee</SelectItem>
                                  <SelectItem value="organizer">Organizer</SelectItem>
                                  <SelectItem value="admin">Admin</SelectItem>
                                </SelectContent>
                              </Select>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {tab === "events" && (
              <div className="rounded-xl border border-border bg-card card-shadow overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-muted/50">
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Event</th>
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Category</th>
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Date</th>
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Attendees</th>
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
                        <th className="px-4 py-3 text-left font-medium text-muted-foreground">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {events.map((ev) => (
                        <tr
                          key={ev.id}
                          className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors"
                        >
                          <td className="px-4 py-3 font-medium text-card-foreground max-w-[200px] truncate">
                            {ev.title}
                          </td>
                          <td className="px-4 py-3">
                            <Badge variant="secondary" className="text-xs">
                              {ev.category}
                            </Badge>
                          </td>
                          <td className="px-4 py-3 text-muted-foreground">{ev.date}</td>
                          <td className="px-4 py-3 text-muted-foreground">
                            {ev.attendees}/{ev.capacity}
                          </td>
                          <td className="px-4 py-3">
                            <Badge className="bg-success/10 text-success border-0">{ev.status}</Badge>
                          </td>
                          <td className="px-4 py-3">
                            <Link to={`/event/${ev.id}`}>
                              <Button variant="ghost" size="sm">
                                View
                              </Button>
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {tab === "tickets" && (
              <div className="space-y-4">
                <p className="text-sm text-muted-foreground">Admin view of all tickets with user details.</p>
                <div className="rounded-xl border border-border bg-card card-shadow overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-border bg-muted/50">
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Ticket Code</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">User</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Event</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Qty</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Total</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
                          <th className="px-4 py-3 text-left font-medium text-muted-foreground">Purchased</th>
                        </tr>
                      </thead>
                      <tbody>
                        {tickets.map((t) => {
                          const user = mockUsers.find((u) => u.id === t.userId);
                          return (
                            <tr
                              key={t.id}
                              className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors"
                            >
                              <td className="px-4 py-3 font-mono text-xs text-card-foreground">{t.ticketCode}</td>
                              <td className="px-4 py-3">
                                <div className="flex items-center gap-2">
                                  {user && <img src={user.avatar} alt={user.name} className="h-6 w-6 rounded-full" />}
                                  <span className="text-card-foreground">{t.userName}</span>
                                </div>
                              </td>
                              <td className="px-4 py-3 text-muted-foreground max-w-[180px] truncate">{t.eventTitle}</td>
                              <td className="px-4 py-3 text-muted-foreground">{t.quantity}</td>
                              <td className="px-4 py-3 font-medium text-card-foreground">
                                {t.totalPrice === 0 ? "Free" : `$${t.totalPrice}`}
                              </td>
                              <td className="px-4 py-3">
                                <Badge className={statusColors[t.status]}>{t.status}</Badge>
                              </td>
                              <td className="px-4 py-3 text-muted-foreground">{t.purchasedAt}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </main>
      <Footer />
    </div>
  );
};

export default AdminDashboard;
