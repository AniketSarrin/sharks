import { Search, CalendarPlus, TicketCheck, Users } from "lucide-react";

const features = [
  {
    icon: Search,
    title: "Easy Event Discovery",
    description: "Find events by category, location, or keyword — all in one place.",
  },
  {
    icon: CalendarPlus,
    title: "Fast Event Creation",
    description: "Launch your event in minutes with our intuitive creation tools.",
  },
  {
    icon: TicketCheck,
    title: "Simple RSVP Tracking",
    description: "Manage registrations, tickets, and attendees effortlessly.",
  },
  {
    icon: Users,
    title: "Community Focused",
    description: "Connect with people who share your interests and passions.",
  },
];

const WhySharks = () => {
  return (
    <section className="py-16 bg-secondary/50">
      <div className="container mx-auto px-4">
        <div className="mb-10 text-center">
          <h2 className="text-3xl font-bold text-foreground">Why Sharks?</h2>
          <p className="mt-2 text-muted-foreground">Everything you need to discover and manage events</p>
        </div>
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((feature, i) => (
            <div
              key={feature.title}
              className="rounded-xl bg-card p-6 text-center card-shadow animate-fade-in"
              style={{ animationDelay: `${i * 0.1}s` }}
            >
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-primary/10">
                <feature.icon className="h-7 w-7 text-primary" />
              </div>
              <h3 className="mb-2 text-lg font-semibold text-card-foreground">{feature.title}</h3>
              <p className="text-sm text-muted-foreground">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default WhySharks;
