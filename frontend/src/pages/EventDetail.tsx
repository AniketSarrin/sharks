import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Calendar, Clock, MapPin, Users, Share2, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { eventsApi } from "@/lib/events-api";
import type { Event } from "@/lib/mock-data";
import { getOrganizerById, type PublicOrganizerDto } from "@/lib/profile-api";
import { toast } from "sonner";

const EventDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState<Event | null>(null);
  const [organizer, setOrganizer] = useState<PublicOrganizerDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    eventsApi.getById(id).then((res) => {
      if (res.success) setEvent(res.data);
      setLoading(false);
    });
  }, [id]);

  useEffect(() => {
    if (!event?.organizerId) return;
    getOrganizerById(event.organizerId).then((res) => {
      if (res.success) setOrganizer(res.data);
    });
  }, [event?.organizerId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto flex flex-col items-center py-20 text-center">
          <p className="text-lg text-muted-foreground">Loading event...</p>
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto flex flex-col items-center py-20 text-center">
          <p className="text-lg text-muted-foreground">Event not found</p>
          <Link to="/">
            <Button variant="outline" className="mt-4">Back to events</Button>
          </Link>
        </div>
      </div>
    );
  }

  const dateObj = new Date(event.date + "T" + event.time);
  const formattedDate = dateObj.toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
  const spotsLeft = event.capacity - event.attendees;
  const fillPercent = event.capacity > 0 ? (event.attendees / event.capacity) * 100 : 0;

  const handleRegister = () => {
    navigate(`/checkout/${event.id}`);
  };

  const handleAddToCalendar = () => {
    const start = event.date.replace(/-/g, "") + "T" + event.time.replace(":", "") + "00";
    const end = event.endTime
      ? event.date.replace(/-/g, "") + "T" + event.endTime.replace(":", "") + "00"
      : start;
    const url = `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${encodeURIComponent(event.title)}&dates=${start}/${end}&location=${encodeURIComponent(event.address)}&details=${encodeURIComponent(event.description)}`;
    window.open(url, "_blank");
  };

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    toast.success("Link copied to clipboard!");
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      {/* Hero image */}
      <div className="relative h-64 md:h-96 w-full overflow-hidden">
        <img
            src={event.image}
            alt={event.title}
            className="h-full w-full object-cover"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).src =
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop";
            }}
          />
        <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/30 to-transparent" />
        <Link to="/" className="absolute left-4 top-4 flex items-center gap-1 rounded-full bg-background/80 px-3 py-1.5 text-sm font-medium text-foreground backdrop-blur-sm hover:bg-background transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back
        </Link>
      </div>

      <main className="container mx-auto px-4 -mt-16 relative z-10 pb-12">
        <div className="grid gap-8 lg:grid-cols-3">
          {/* Main content */}
          <div className="lg:col-span-2">
            <div className="rounded-xl border border-border bg-card p-6 md:p-8 card-shadow">
              <div className="mb-4 flex flex-wrap gap-2">
                <Badge className="bg-primary/10 text-primary border-0">{event.category}</Badge>
                {event.price === 0 && <Badge className="bg-success/10 text-success border-0">Free</Badge>}
                <Badge variant="outline">{event.status}</Badge>
              </div>

              <h1 className="mb-4 text-3xl font-extrabold text-card-foreground md:text-4xl">{event.title}</h1>

              <div className="mb-6 grid gap-3 text-muted-foreground sm:grid-cols-2">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-primary" />
                  <span className="text-sm">{formattedDate}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-primary" />
                  <span className="text-sm">{event.time} – {event.endTime}</span>
                </div>
                <div className="flex items-center gap-2">
                  <MapPin className="h-4 w-4 text-primary" />
                  <span className="text-sm">{event.location}, {event.city}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4 text-primary" />
                  <span className="text-sm">{event.attendees} / {event.capacity} attending</span>
                </div>
              </div>

              <div className="border-t border-border pt-6">
                <h2 className="mb-3 text-xl font-bold text-card-foreground">About this event</h2>
                <p className="leading-relaxed text-muted-foreground">{event.description}</p>
              </div>

              <div className="mt-6 flex flex-wrap gap-2">
                {event.tags.map((tag) => (
                  <Badge key={tag} variant="secondary" className="text-xs">#{tag}</Badge>
                ))}
              </div>

              {/* Organizer */}
              <div className="mt-8 border-t border-border pt-6">
                <h2 className="mb-4 text-xl font-bold text-card-foreground">Organizer</h2>
                <div className="flex items-center gap-4">
                  <img
                    src={
                      organizer?.avatarUrl ||
                      `https://ui-avatars.com/api/?name=${encodeURIComponent(organizer?.fullName || "O")}&background=e8553a&color=fff`
                    }
                    alt={organizer?.fullName || "Organizer"}
                    className="h-12 w-12 rounded-full"
                  />
                  <div>
                    <p className="font-semibold text-card-foreground">
                      {organizer?.fullName || "Loading…"}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {organizer?.organizerDescription || organizer?.bio || ""}
                    </p>
                  </div>
                </div>
              </div>

              {/* Map placeholder */}
              <div className="mt-8 border-t border-border pt-6">
                <h2 className="mb-4 text-xl font-bold text-card-foreground">Location</h2>
                <div className="overflow-hidden rounded-lg border border-border">
                  <iframe
                    title="Event location"
                    src={`https://maps.google.com/maps?q=${encodeURIComponent(event.address + ", " + event.city)}&output=embed`}
                    className="h-64 w-full border-0"
                    loading="lazy"
                  />
                </div>
                <p className="mt-2 text-sm text-muted-foreground">{event.address}, {event.city}</p>
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1">
            <div className="sticky top-20 rounded-xl border border-border bg-card p-6 card-shadow">
              <div className="mb-4 text-center">
                <p className="text-3xl font-extrabold text-card-foreground">
                  {event.price === 0 ? "Free" : `$${event.price}`}
                </p>
                {event.price > 0 && <p className="text-sm text-muted-foreground">per ticket</p>}
              </div>

              <div className="mb-4">
                <div className="mb-1 flex justify-between text-sm">
                  <span className="text-muted-foreground">{event.attendees} registered</span>
                  <span className="font-medium text-card-foreground">{spotsLeft} left</span>
                </div>
                <Progress value={fillPercent} className="h-2" />
              </div>

              <Button className="mb-3 w-full" size="lg" onClick={handleRegister} disabled={spotsLeft <= 0}>
                {spotsLeft > 0 ? (event.price === 0 ? "Register for Free" : "Get Tickets") : "Sold Out"}
              </Button>

              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" size="sm" onClick={handleAddToCalendar}>
                  <Calendar className="mr-1 h-4 w-4" /> Calendar
                </Button>
                <Button variant="outline" className="flex-1" size="sm" onClick={handleShare}>
                  <Share2 className="mr-1 h-4 w-4" /> Share
                </Button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default EventDetail;
