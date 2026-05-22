import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { eventsApi } from "@/lib/events-api";
import type { BackendEventType } from "@/lib/api-types";
import { getAccessToken } from "@/lib/auth-storage";
import { toast } from "sonner";
import { CalendarDays, MapPin, Users, Loader2, UploadCloud, X } from "lucide-react";

const CATEGORY_TO_TYPE: Record<string, BackendEventType> = {
  Music: "MUSIC",
  Networking: "NETWORKING",
  Dating: "DATING",
};
const CREATE_EVENT_CATEGORIES = Object.keys(CATEGORY_TO_TYPE);

const CreateEvent = () => {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [form, setForm] = useState({
    title: "",
    description: "",
    date: "",
    time: "",
    endTime: "",
    location: "",
    address: "",
    city: "",
    category: "",
    capacity: "",
    price: "",
    singer: "",
    minAge: "",
    maxAge: "",
  });

  useEffect(() => {
    if (!getAccessToken()) {
      toast.error("You must be logged in to create an event");
      navigate("/login");
    }
  }, [navigate]);

  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));

  const applyImageFile = (file: File) => {
    if (file.type !== "image/jpeg" && file.type !== "image/jpg") {
      toast.error("Only JPEG images are supported");
      return;
    }
    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
  };

  const clearImage = () => {
    if (imagePreview) URL.revokeObjectURL(imagePreview);
    setImageFile(null);
    setImagePreview(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.title || !form.date || !form.time || !form.location || !form.category || !form.capacity || !form.price) {
      toast.error("Please fill in all required fields");
      return;
    }

    const eventTime = new Date(`${form.date}T${form.time}:00`).toISOString();
    const address = [form.location, form.address, form.city].filter(Boolean).join(", ");
    const eventType = CATEGORY_TO_TYPE[form.category];
    if (!eventType) {
      toast.error("Please select a valid category");
      return;
    }

    setSubmitting(true);
    const res = await eventsApi.create(
      {
        name: form.title,
        address,
        eventTime,
        ticketsProvisioned: Number(form.capacity),
        price: Number(form.price),
        description: form.description || undefined,
        type: eventType,
        singer: form.singer || undefined,
        minAge: form.minAge ? Number(form.minAge) : undefined,
        maxAge: form.maxAge ? Number(form.maxAge) : undefined,
      },
      imageFile ?? undefined
    );
    setSubmitting(false);

    if (res.success) {
      toast.success("Event created!", { description: `"${res.data.title}" is now live.` });
      navigate(`/event/${res.data.id}`);
    } else {
      toast.error("Failed to create event", { description: res.error });
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto max-w-3xl px-4 py-12">
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-foreground">Create Event</h1>
          <p className="mt-1 text-muted-foreground">Fill in the details to publish your event.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Basic info */}
          <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
            <h2 className="text-lg font-bold text-card-foreground">Basic Information</h2>
            <div>
              <Label htmlFor="title">Event Title *</Label>
              <Input id="title" placeholder="e.g. Summer Music Festival" value={form.title} onChange={(e) => update("title", e.target.value)} className="mt-1.5" />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea id="description" rows={5} placeholder="Tell people what your event is about..." value={form.description} onChange={(e) => update("description", e.target.value)} className="mt-1.5" />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <Label htmlFor="category">Category *</Label>
                <Select value={form.category} onValueChange={(v) => update("category", v)}>
                  <SelectTrigger className="mt-1.5"><SelectValue placeholder="Select category" /></SelectTrigger>
                  <SelectContent>
                    {CREATE_EVENT_CATEGORIES.map((c) => <SelectItem key={c} value={c}>{c}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label>Event Image</Label>
                <div className="mt-1.5">
                  {imagePreview ? (
                    <div className="relative w-full overflow-hidden rounded-lg border border-border">
                      <img src={imagePreview} alt="Preview" className="h-36 w-full object-cover" />
                      <button
                        type="button"
                        onClick={clearImage}
                        className="absolute right-2 top-2 rounded-full bg-black/60 p-1 text-white hover:bg-black/80"
                        aria-label="Remove image"
                      >
                        <X className="h-3.5 w-3.5" />
                      </button>
                      <p className="truncate px-2 py-1.5 text-xs text-muted-foreground">{imageFile?.name}</p>
                    </div>
                  ) : (
                    <div
                      role="button"
                      tabIndex={0}
                      onClick={() => fileInputRef.current?.click()}
                      onKeyDown={(e) => e.key === "Enter" && fileInputRef.current?.click()}
                      onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                      onDragLeave={() => setDragOver(false)}
                      onDrop={(e) => {
                        e.preventDefault();
                        setDragOver(false);
                        const file = e.dataTransfer.files[0];
                        if (file) applyImageFile(file);
                      }}
                      className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed px-4 py-6 transition-colors ${dragOver ? "border-primary bg-primary/5" : "border-border hover:border-primary/60 hover:bg-muted/40"}`}
                    >
                      <UploadCloud className="h-6 w-6 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">
                        Drop an image here or <span className="font-medium text-primary">browse</span>
                      </span>
                      <span className="text-xs text-muted-foreground">JPEG only</span>
                    </div>
                  )}
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/jpeg"
                    className="hidden"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) applyImageFile(file);
                    }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Date & Time */}
          <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
            <h2 className="flex items-center gap-2 text-lg font-bold text-card-foreground">
              <CalendarDays className="h-5 w-5 text-primary" /> Date & Time
            </h2>
            <div className="grid gap-4 sm:grid-cols-3">
              <div>
                <Label htmlFor="date">Date *</Label>
                <Input id="date" type="date" value={form.date} onChange={(e) => update("date", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="time">Start Time *</Label>
                <Input id="time" type="time" value={form.time} onChange={(e) => update("time", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="endTime">End Time</Label>
                <Input id="endTime" type="time" value={form.endTime} onChange={(e) => update("endTime", e.target.value)} className="mt-1.5" />
              </div>
            </div>
          </div>

          {/* Location */}
          <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
            <h2 className="flex items-center gap-2 text-lg font-bold text-card-foreground">
              <MapPin className="h-5 w-5 text-primary" /> Location
            </h2>
            <div>
              <Label htmlFor="location">Venue Name *</Label>
              <Input id="location" placeholder="e.g. Golden Gate Park" value={form.location} onChange={(e) => update("location", e.target.value)} className="mt-1.5" />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <Label htmlFor="address">Address</Label>
                <Input id="address" placeholder="Street address" value={form.address} onChange={(e) => update("address", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="city">City</Label>
                <Input id="city" placeholder="e.g. San Francisco, CA" value={form.city} onChange={(e) => update("city", e.target.value)} className="mt-1.5" />
              </div>
            </div>
          </div>

          {/* Tickets */}
          <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
            <h2 className="flex items-center gap-2 text-lg font-bold text-card-foreground">
              <Users className="h-5 w-5 text-primary" /> Tickets & Capacity
            </h2>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <Label htmlFor="capacity">Capacity</Label>
                <Input id="capacity" type="number" min="1" placeholder="Max attendees" value={form.capacity} onChange={(e) => update("capacity", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="price">Price ($)</Label>
                <Input id="price" type="number" min="0" step="0.01" placeholder="0 for free" value={form.price} onChange={(e) => update("price", e.target.value)} className="mt-1.5" />
              </div>
            </div>
          </div>

          {/* Optional fields for MUSIC events */}
          {form.category === "Music" && (
            <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
              <h2 className="text-lg font-bold text-card-foreground">Music Details</h2>
              <div>
                <Label htmlFor="singer">Featured Artist</Label>
                <Input id="singer" placeholder="e.g. Taylor Swift" value={form.singer} onChange={(e) => update("singer", e.target.value)} className="mt-1.5" />
              </div>
            </div>
          )}

          {/* Age restrictions */}
          <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-5">
            <h2 className="text-lg font-bold text-card-foreground">Age Restrictions (optional)</h2>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <Label htmlFor="minAge">Min Age</Label>
                <Input id="minAge" type="number" min="0" placeholder="e.g. 18" value={form.minAge} onChange={(e) => update("minAge", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="maxAge">Max Age</Label>
                <Input id="maxAge" type="number" min="0" placeholder="e.g. 65" value={form.maxAge} onChange={(e) => update("maxAge", e.target.value)} className="mt-1.5" />
              </div>
            </div>
          </div>

          <div className="flex gap-3 justify-end">
            <Button type="button" variant="outline" onClick={() => navigate("/")} disabled={submitting}>Cancel</Button>
            <Button type="submit" size="lg" disabled={submitting}>
              {submitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Creating…</> : "Create Event"}
            </Button>
          </div>
        </form>
      </main>
      <Footer />
    </div>
  );
};

export default CreateEvent;
