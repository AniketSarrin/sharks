import { Link } from "react-router-dom";
import { MapPin, Users } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Event } from "@/lib/mock-data";

const EventCard = ({ event }: { event: Event }) => {
  const dateObj = new Date(event.date + "T" + event.time);
  const formattedDate = dateObj.toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  });
  const spotsLeft = event.capacity - event.attendees;
  const isLimited = spotsLeft > 0 && spotsLeft <= event.capacity * 0.2;

  return (
    <Link to={`/event/${event.id}`} className="group block">
      <div className="overflow-hidden rounded-xl border border-border bg-card card-shadow transition-all duration-300 hover:card-shadow-hover hover:-translate-y-1">
        <div className="relative aspect-[16/9] overflow-hidden">
          <img
            src={event.image}
            alt={event.title}
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).src =
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop";
            }}
          />
          <div className="absolute left-3 top-3 flex flex-wrap gap-2">
            <Badge className="bg-primary/90 text-primary-foreground backdrop-blur-sm border-0 text-xs font-medium">
              {event.category}
            </Badge>
            {event.price === 0 && (
              <Badge className="bg-success text-success-foreground border-0 text-xs font-medium">
                Free
              </Badge>
            )}
            {isLimited && (
              <Badge className="bg-warning text-warning-foreground border-0 text-xs font-medium">
                Limited spots
              </Badge>
            )}
          </div>
          {event.isFeatured && (
            <Badge className="absolute right-3 top-3 hero-gradient border-0 text-primary-foreground text-xs">
              Featured
            </Badge>
          )}
        </div>
        <div className="p-4">
          <p className="mb-1 text-sm font-semibold text-primary">
            {formattedDate} · {event.time}
          </p>
          <h3 className="mb-2 text-lg font-bold leading-tight text-card-foreground line-clamp-2 group-hover:text-primary transition-colors">
            {event.title}
          </h3>
          <div className="mb-3 flex items-center gap-1 text-sm text-muted-foreground">
            <MapPin className="h-3.5 w-3.5 shrink-0" />
            <span className="truncate">{event.location} · {event.city}</span>
          </div>
          <div className="flex items-center justify-between border-t border-border pt-3">
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1 text-sm text-muted-foreground">
                <Users className="h-3.5 w-3.5" />
                <span>{spotsLeft > 0 ? `${spotsLeft} spots left` : "Sold out"}</span>
              </div>
              {event.price > 0 && (
                <span className="text-sm font-semibold text-card-foreground">
                  ${event.price}
                </span>
              )}
            </div>
            <Button size="sm" variant="outline" className="border-primary text-primary text-xs hover:bg-primary hover:text-primary-foreground">
              {spotsLeft > 0 ? "Register" : "Sold out"}
            </Button>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default EventCard;
