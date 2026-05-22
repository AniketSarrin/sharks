import { useState, useEffect, useMemo, useCallback } from "react";
import Navbar from "@/components/NavBar";
import HeroSection from "@/components/HeroSection";
import CategoryShowcase from "@/components/CategoryShowcase";
import CategoryFilter from "@/components/CategoryFilter";
import EventCard from "@/components/EventCard";
import WhySharks from "@/components/WhySharks";
import RoleCTA from "@/components/RoleCTA";
import Footer from "@/components/Footer";
import { eventsApi } from "@/lib/events-api";
import type { Event } from "@/lib/mock-data";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

const Index = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [locationQuery, setLocationQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [sortBy, setSortBy] = useState("date");
  const [allEvents, setAllEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchEvents = useCallback(async () => {
    setLoading(true);
    const res = await eventsApi.getAll({ size: 100 });
    if (res.success) setAllEvents(res.data);
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const filteredEvents = useMemo(() => {
    let events = allEvents.filter((e) => {
      const matchesSearch =
        !searchQuery ||
        e.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        e.tags.some((t) => t.toLowerCase().includes(searchQuery.toLowerCase()));
      const matchesLocation =
        !locationQuery ||
        e.location.toLowerCase().includes(locationQuery.toLowerCase()) ||
        e.city.toLowerCase().includes(locationQuery.toLowerCase());
      const matchesCategory =
        !selectedCategory || selectedCategory === "all" || e.category === selectedCategory;
      return matchesSearch && matchesLocation && matchesCategory;
    });

    events.sort((a, b) => {
      if (sortBy === "date") return new Date(a.date).getTime() - new Date(b.date).getTime();
      if (sortBy === "price") return a.price - b.price;
      if (sortBy === "popularity") return b.attendees - a.attendees;
      return 0;
    });

    return events;
  }, [allEvents, searchQuery, locationQuery, selectedCategory, sortBy]);

  const featuredEvents = allEvents.slice(0, 3);

  const handleCategorySelect = (cat: string) => {
    setSelectedCategory(cat);
    document.getElementById("events")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <HeroSection
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        locationQuery={locationQuery}
        onLocationChange={setLocationQuery}
        selectedCategory={selectedCategory}
        onCategoryChange={(val) => setSelectedCategory(val === "all" ? "" : val)}
      />

      {/* Category Showcase */}
      <CategoryShowcase onSelect={handleCategorySelect} />

      {/* Events Section */}
      <main id="events" className="container mx-auto px-4 py-12">
        {/* Featured section */}
        {!searchQuery && !locationQuery && !selectedCategory && (
          <section className="mb-12">
            <h2 className="mb-6 text-2xl font-bold text-foreground">Featured Events</h2>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {featuredEvents.map((event, i) => (
                <div key={event.id} className="animate-fade-in" style={{ animationDelay: `${i * 0.1}s` }}>
                  <EventCard event={event} />
                </div>
              ))}
            </div>
          </section>
        )}

        {/* All events */}
        <section>
          <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="text-2xl font-bold text-foreground">
              {selectedCategory || "All"} Events
              <span className="ml-2 text-base font-normal text-muted-foreground">
                ({filteredEvents.length})
              </span>
            </h2>
            <Select value={sortBy} onValueChange={setSortBy}>
              <SelectTrigger className="w-[160px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="date">Date</SelectItem>
                <SelectItem value="price">Price</SelectItem>
                <SelectItem value="popularity">Popularity</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <CategoryFilter selected={selectedCategory} onSelect={setSelectedCategory} />

          {loading ? (
            <div className="mt-6 py-20 text-center text-muted-foreground">Loading events...</div>
          ) : (
            <>
              <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {filteredEvents.map((event, i) => (
                  <div key={event.id} className="animate-fade-in" style={{ animationDelay: `${i * 0.05}s` }}>
                    <EventCard event={event} />
                  </div>
                ))}
              </div>

              {filteredEvents.length === 0 && (
                <div className="py-20 text-center">
                  <p className="text-lg font-medium text-muted-foreground">No events found</p>
                  <p className="text-sm text-muted-foreground">Try adjusting your search or filters</p>
                </div>
              )}
            </>
          )}
        </section>
      </main>

      {/* Why Sharks */}
      <WhySharks />

      {/* Role-based CTA */}
      <RoleCTA />

      <Footer />
    </div>
  );
};

export default Index;
