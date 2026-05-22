import { Link } from "react-router-dom";
import { Search, CalendarPlus, Shield } from "lucide-react";
import { Button } from "@/components/ui/button";

const roles = [
  {
    icon: Search,
    title: "For Attendees",
    description: "Discover events near you, register with one click, and keep track of your upcoming plans.",
    cta: "Explore Events",
    to: "/",
  },
  {
    icon: CalendarPlus,
    title: "For Organizers",
    description: "Create and manage your own events easily. Track registrations, set capacity, and reach your audience.",
    cta: "Create Event",
    to: "/create",
  },
  {
    icon: Shield,
    title: "For Admins",
    description: "Oversee the platform, manage users and events, and ensure everything runs smoothly.",
    cta: "Admin Dashboard",
    to: "/admin",
  },
];

const RoleCTA = () => {
  return (
    <section className="py-16 bg-secondary/50">
      <div className="container mx-auto px-4">
        <div className="mb-10 text-center">
          <h2 className="text-3xl font-bold text-foreground">Built for Everyone</h2>
          <p className="mt-2 text-muted-foreground">Whether you're attending, organizing, or managing — Sharks has you covered</p>
        </div>
        <div className="grid gap-6 md:grid-cols-3">
          {roles.map((role, i) => (
            <div
              key={role.title}
              className="rounded-xl bg-card p-8 card-shadow text-center animate-fade-in"
              style={{ animationDelay: `${i * 0.1}s` }}
            >
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-primary/10">
                <role.icon className="h-7 w-7 text-primary" />
              </div>
              <h3 className="mb-2 text-xl font-semibold text-card-foreground">{role.title}</h3>
              <p className="mb-6 text-sm text-muted-foreground">{role.description}</p>
              <Link to={role.to}>
                <Button variant="outline" className="border-primary text-primary hover:bg-primary hover:text-primary-foreground">
                  {role.cta}
                </Button>
              </Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default RoleCTA;
