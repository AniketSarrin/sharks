import { Search, MapPin } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { categories } from "@/lib/mock-data";

interface HeroSectionProps {
  searchQuery: string;
  onSearchChange: (q: string) => void;
  locationQuery: string;
  onLocationChange: (q: string) => void;
  selectedCategory: string;
  onCategoryChange: (cat: string) => void;
}

const HeroSection = ({
  searchQuery,
  onSearchChange,
  locationQuery,
  onLocationChange,
  selectedCategory,
  onCategoryChange,
}: HeroSectionProps) => {
  return (
    <section className="relative overflow-hidden hero-gradient py-20 md:py-28">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_50%,rgba(255,255,255,0.1),transparent_60%)]" />
      <div className="container relative mx-auto px-4 text-center">
        <h1 className="mb-4 text-4xl font-extrabold tracking-tight text-primary-foreground md:text-6xl animate-fade-in">
          Dive Into Your Next Event
        </h1>
        <p className="mx-auto mb-4 max-w-2xl text-lg text-primary-foreground/80 animate-fade-in" style={{ animationDelay: "0.1s" }}>
          Discover, create, and attend events with ease — all in one place.
        </p>
        <p className="mx-auto mb-8 max-w-2xl text-sm text-primary-foreground/60 animate-fade-in" style={{ animationDelay: "0.15s" }}>
          Sharks is a modern event platform where people can discover exciting events, organizers can create and manage them, and communities can connect through shared experiences. From workshops and meetups to concerts and family festivals, Sharks makes every event easy to explore, join, and organize.
        </p>

        {/* CTA Buttons */}
        <div className="mb-10 flex items-center justify-center gap-4 animate-fade-in" style={{ animationDelay: "0.15s" }}>
          <Link to="/#events">
            <Button size="lg" className="h-12 px-8 bg-primary text-primary-foreground hover:bg-primary/90">
              Explore Events
            </Button>
          </Link>
          <Link to="/create">
            <Button size="lg" variant="outline" className="h-12 px-8 border-2 border-primary-foreground bg-primary-foreground/10 text-primary-foreground hover:bg-primary-foreground hover:text-primary">
              Create Event
            </Button>
          </Link>
        </div>

        {/* Search Bar */}
        <div className="mx-auto flex max-w-3xl flex-col gap-3 rounded-xl bg-card/95 p-4 shadow-lg backdrop-blur-sm sm:flex-row sm:items-center animate-fade-in" style={{ animationDelay: "0.2s" }}>
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search events..."
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              className="h-11 border-border bg-background pl-10 text-foreground"
            />
          </div>
          <div className="relative flex-1">
            <MapPin className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Location..."
              value={locationQuery}
              onChange={(e) => onLocationChange(e.target.value)}
              className="h-11 border-border bg-background pl-10 text-foreground"
            />
          </div>
          <Select value={selectedCategory} onValueChange={onCategoryChange}>
            <SelectTrigger className="h-11 w-full sm:w-[180px] border-border bg-background text-foreground">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Categories</SelectItem>
              {categories.map((cat) => (
                <SelectItem key={cat} value={cat}>{cat}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button size="lg" className="h-11 px-6 bg-primary text-primary-foreground hover:bg-primary/90">
            Search
          </Button>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
