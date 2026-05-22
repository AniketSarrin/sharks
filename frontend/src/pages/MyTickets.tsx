import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ticketingApi } from "@/lib/ticketing-api";
import type { Ticket } from "@/lib/api-types";
import { Calendar, Clock, MapPin, Ticket as TicketIcon, QrCode, RotateCcw } from "lucide-react";
import { toast } from "sonner";

const statusColors: Record<string, string> = {
  confirmed: "bg-success/10 text-success border-0",
  pending: "bg-warning/10 text-warning border-0",
  refunded: "bg-muted text-muted-foreground border-0",
  cancelled: "bg-destructive/10 text-destructive border-0",
};

const MyTickets = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    ticketingApi.getMyTickets().then((res) => {
      if (res.success) setTickets(res.data);
      setLoading(false);
    });
  }, []);

  const handleRefund = async (ticketId: string) => {
    // Optimistic update — refund endpoint not yet on the ticketing service
    setTickets((prev) => prev.map((t) => (t.id === ticketId ? { ...t, status: "refunded" as const } : t)));
    toast.success("Refund requested");
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto max-w-4xl px-4 py-12">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-extrabold text-foreground">My Tickets</h1>
            <p className="mt-1 text-muted-foreground">
              <code className="rounded bg-muted px-1.5 py-0.5 text-xs">GET /api/tickets/my-tickets</code>
            </p>
          </div>
          <Badge variant="secondary">{tickets.length} tickets</Badge>
        </div>

        {loading ? (
          <div className="py-20 text-center text-muted-foreground">Loading tickets...</div>
        ) : tickets.length === 0 ? (
          <div className="py-20 text-center">
            <TicketIcon className="mx-auto mb-4 h-12 w-12 text-muted-foreground/40" />
            <p className="text-lg font-medium text-muted-foreground">No tickets yet</p>
            <Link to="/">
              <Button className="mt-4">Browse Events</Button>
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {tickets.map((ticket) => (
              <div key={ticket.id} className="rounded-xl border border-border bg-card p-5 card-shadow transition-all hover:card-shadow-hover">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div className="flex-1">
                    <div className="mb-2 flex items-center gap-2">
                      <Badge className={statusColors[ticket.status]}>{ticket.status}</Badge>
                      <span className="text-xs text-muted-foreground font-mono">{ticket.ticketCode}</span>
                    </div>
                    <h3 className="text-lg font-bold text-card-foreground">{ticket.eventTitle}</h3>
                    <div className="mt-2 grid gap-1.5 text-sm text-muted-foreground sm:grid-cols-2">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="h-3.5 w-3.5 text-primary" />
                        {new Date(ticket.eventDate).toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric", year: "numeric" })}
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock className="h-3.5 w-3.5 text-primary" />
                        {ticket.eventTime}
                      </div>
                      <div className="flex items-center gap-1.5 sm:col-span-2">
                        <MapPin className="h-3.5 w-3.5 text-primary" />
                        {ticket.eventLocation}
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-2 text-right">
                    <div>
                      <p className="text-2xl font-extrabold text-card-foreground">
                        {ticket.totalPrice === 0 ? "Free" : `$${ticket.totalPrice}`}
                      </p>
                      <p className="text-xs text-muted-foreground">{ticket.quantity} ticket{ticket.quantity > 1 ? "s" : ""}</p>
                    </div>
                    <div className="flex gap-2">
                      <Link to={`/event/${ticket.eventId}`}>
                        <Button variant="outline" size="sm">View Event</Button>
                      </Link>
                      {ticket.status === "confirmed" && (
                        <Button variant="outline" size="sm" onClick={() => handleRefund(ticket.id)}>
                          <RotateCcw className="mr-1 h-3.5 w-3.5" /> Refund
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};

export default MyTickets;
