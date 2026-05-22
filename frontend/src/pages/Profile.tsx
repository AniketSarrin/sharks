import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { usersApi, customerApi } from "@/lib/api";
import {
  deleteMyRoleProfile,
  getEmailConfirmedFromAccessToken,
  loadFullProfile,
  patchMyRoleProfile,
} from "@/lib/profile-api";
import type { BackendUserRole, RoleProfileDto, RoleProfilePatchBody } from "@/lib/api-types";
import { roleProfilePathSegment } from "@/lib/api-types";
import type { Event } from "@/lib/mock-data";
import { Calendar, MapPin, Trash2, Save } from "lucide-react";
import { toast } from "sonner";
import EventCard from "@/components/EventCard";

interface EditFormState {
  fullName: string;
  email: string;
  avatarUrl: string;
  bio: string;
  phone: string;
  location: string;
  nickname: string;
  organizerDescription: string;
}

function formFromProfile(p: RoleProfileDto): EditFormState {
  return {
    fullName: p.fullName,
    email: p.email,
    avatarUrl: p.avatarUrl ?? "",
    bio: p.bio ?? "",
    phone: p.phone ?? "",
    location: p.location ?? "",
    nickname: p.nickname ?? "",
    organizerDescription: p.organizerDescription ?? "",
  };
}

function buildPatch(prev: RoleProfileDto, form: EditFormState): RoleProfilePatchBody | "no-changes" | "invalid-name" {
  const patch: Record<string, string> = {};
  const fullName = form.fullName.trim();
  if (fullName !== prev.fullName.trim()) {
    if (!fullName) return "invalid-name";
    patch.fullName = fullName;
  }
  const avatarUrl = form.avatarUrl.trim();
  if (avatarUrl !== (prev.avatarUrl ?? "").trim()) {
    patch.avatarUrl = avatarUrl;
  }
  const bio = form.bio.trim();
  if (bio !== (prev.bio ?? "").trim()) {
    patch.bio = bio;
  }
  const phone = form.phone.trim();
  if (phone !== (prev.phone ?? "").trim()) {
    patch.phone = phone;
  }
  const location = form.location.trim();
  if (location !== (prev.location ?? "").trim()) {
    patch.location = location;
  }
  if (prev.role === "attendee") {
    const nickname = form.nickname.trim();
    if (nickname !== (prev.nickname ?? "").trim()) {
      patch.nickname = nickname;
    }
  }
  if (prev.role === "organizer") {
    const od = form.organizerDescription.trim();
    if (od !== (prev.organizerDescription ?? "").trim()) {
      patch.organizerDescription = od;
    }
  }
  if (Object.keys(patch).length === 0) return "no-changes";
  return patch as RoleProfilePatchBody;
}

function roleLabel(role: BackendUserRole): string {
  return role.charAt(0).toUpperCase() + role.slice(1);
}

const Profile = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<RoleProfileDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<EditFormState | null>(null);
  const [emailConfirmed, setEmailConfirmed] = useState<boolean | null>(null);
  const [eventsCreated, setEventsCreated] = useState<Event[]>([]);
  const [eventsAttending, setEventsAttending] = useState<Event[]>([]);
  const [eventsHistory, setEventsHistory] = useState<(Event & { ticketStatus?: string })[]>([]);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoadError(null);
    (async () => {
      try {
        const [meRes, createdRes, attendingRes, historyRes] = await Promise.all([
          loadFullProfile(),
          usersApi.getMyEventsCreated(),
          customerApi.getEventsAttending(),
          customerApi.getEventsHistory(),
        ]);
        if (cancelled) return;
        if (!meRes.success) {
          setLoadError(meRes.error);
          if (meRes.error.includes("401") || /unauthorized/i.test(meRes.error)) {
            toast.error("Session expired — please sign in again.");
            navigate("/login");
          } else {
            toast.error(meRes.error || "Could not load profile");
          }
          return;
        }
        setProfile(meRes.data);
        setEditForm(formFromProfile(meRes.data));
        setEmailConfirmed(getEmailConfirmedFromAccessToken());
        if (createdRes.success) setEventsCreated(createdRes.data);
        if (attendingRes.success) setEventsAttending(attendingRes.data);
        if (historyRes.success) setEventsHistory(historyRes.data);
      } catch (err) {
        if (cancelled) return;
        const msg = err instanceof Error ? err.message : "Unexpected error loading profile";
        setLoadError(msg);
        toast.error(msg);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [navigate]);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!profile || !editForm) return;
    const built = buildPatch(profile, editForm);
    if (built === "invalid-name") {
      toast.error("Full name cannot be empty");
      return;
    }
    if (built === "no-changes") {
      toast.message("No changes to save");
      return;
    }
    const res = await patchMyRoleProfile(profile.role, built);
    if (!res.success) {
      toast.error(res.error || "Update failed");
      return;
    }
    setProfile(res.data);
    setEditForm(formFromProfile(res.data));
    toast.success("Profile updated");
  };

  const handleDeleteAccount = async () => {
    if (!profile) return;
    const res = await deleteMyRoleProfile(profile.role);
    if (!res.success) {
      toast.error(res.error || "Could not delete account");
      return;
    }
    toast.success("Account deleted");
    setDeleteDialogOpen(false);
    navigate("/login");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto py-20 text-center text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (loadError && !profile) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto max-w-4xl px-4 py-20 text-center text-muted-foreground">
          <p className="text-destructive">{loadError}</p>
          <Button className="mt-4" variant="outline" onClick={() => navigate("/login")}>
            Go to login
          </Button>
        </div>
        <Footer />
      </div>
    );
  }

  if (!profile || !editForm) return null;

  const segment = roleProfilePathSegment(profile.role);
  const avatarSrc =
    editForm.avatarUrl.trim() ||
    `https://ui-avatars.com/api/?name=${encodeURIComponent(editForm.fullName || "User")}&background=e8553a&color=fff`;

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto max-w-4xl px-4 py-12">
        <h1 className="mb-8 text-3xl font-extrabold text-foreground">My Profile</h1>

        <Tabs defaultValue="profile" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="profile">Profile</TabsTrigger>
            <TabsTrigger value="attending">Attending</TabsTrigger>
            <TabsTrigger value="created">Created</TabsTrigger>
            <TabsTrigger value="history">History</TabsTrigger>
          </TabsList>

          <TabsContent value="profile">
            <div className="rounded-xl border border-border bg-card p-6 card-shadow space-y-6">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h2 className="text-lg font-bold text-card-foreground">Account Details</h2>
                <div className="flex flex-wrap gap-2">
                  <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">GET /api/v1/users/me</code>
                  <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">
                    GET /api/v1/{segment}/me
                  </code>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <img src={avatarSrc} alt={editForm.fullName} className="h-16 w-16 rounded-full object-cover" />
                <div>
                  <p className="text-xl font-bold text-card-foreground">{editForm.fullName}</p>
                  <div className="mt-1 flex flex-wrap items-center gap-2">
                    <Badge className="bg-primary/10 text-primary border-0">{roleLabel(profile.role)}</Badge>
                    <Badge
                      variant="outline"
                      className={
                        emailConfirmed === true
                          ? "bg-success/10 text-success border-0"
                          : emailConfirmed === false
                            ? "bg-warning/10 text-warning border-0"
                            : "bg-muted text-muted-foreground border-0"
                      }
                    >
                      {emailConfirmed === true ? "Email verified" : emailConfirmed === false ? "Email unverified" : "Email status unknown"}
                    </Badge>
                    <Badge variant="outline" className={profile.active ? "border-success/30 text-success" : "text-muted-foreground"}>
                      {profile.active ? "Active" : "Inactive"}
                    </Badge>
                  </div>
                  <p className="mt-2 text-xs text-muted-foreground">
                    Member since {profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : "—"}
                  </p>
                </div>
              </div>

              <form onSubmit={handleUpdateProfile} className="space-y-4 border-t border-border pt-6">
                <p className="text-xs text-muted-foreground">
                  <code className="rounded bg-muted px-1.5 py-0.5">PATCH /api/v1/{segment}/me</code>
                </p>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="sm:col-span-2">
                    <Label htmlFor="fullName">Full name</Label>
                    <Input
                      id="fullName"
                      value={editForm.fullName}
                      onChange={(e) => setEditForm((f) => (f ? { ...f, fullName: e.target.value } : f))}
                      className="mt-1.5"
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="email">Email</Label>
                    <Input id="email" type="email" value={editForm.email} readOnly className="mt-1.5 bg-muted/50" />
                    <p className="mt-1 text-xs text-muted-foreground">Email cannot be changed here.</p>
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="avatarUrl">Avatar URL</Label>
                    <Input
                      id="avatarUrl"
                      value={editForm.avatarUrl}
                      onChange={(e) => setEditForm((f) => (f ? { ...f, avatarUrl: e.target.value } : f))}
                      className="mt-1.5"
                      placeholder="https://..."
                    />
                  </div>
                  <div>
                    <Label htmlFor="phone">Phone</Label>
                    <Input
                      id="phone"
                      value={editForm.phone}
                      onChange={(e) => setEditForm((f) => (f ? { ...f, phone: e.target.value } : f))}
                      className="mt-1.5"
                    />
                  </div>
                  <div>
                    <Label htmlFor="location">Location</Label>
                    <Input
                      id="location"
                      value={editForm.location}
                      onChange={(e) => setEditForm((f) => (f ? { ...f, location: e.target.value } : f))}
                      className="mt-1.5"
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="bio">Bio</Label>
                    <Textarea
                      id="bio"
                      value={editForm.bio}
                      onChange={(e) => setEditForm((f) => (f ? { ...f, bio: e.target.value } : f))}
                      className="mt-1.5 min-h-[88px]"
                    />
                  </div>
                  {profile.role === "attendee" && (
                    <div className="sm:col-span-2">
                      <Label htmlFor="nickname">Nickname</Label>
                      <Input
                        id="nickname"
                        value={editForm.nickname}
                        onChange={(e) => setEditForm((f) => (f ? { ...f, nickname: e.target.value } : f))}
                        className="mt-1.5"
                      />
                    </div>
                  )}
                  {profile.role === "organizer" && (
                    <div className="sm:col-span-2">
                      <Label htmlFor="organizerDescription">Organizer description</Label>
                      <Textarea
                        id="organizerDescription"
                        value={editForm.organizerDescription}
                        onChange={(e) =>
                          setEditForm((f) => (f ? { ...f, organizerDescription: e.target.value } : f))
                        }
                        className="mt-1.5 min-h-[88px]"
                      />
                    </div>
                  )}
                </div>
                <div className="flex gap-2">
                  <Button type="submit" size="sm">
                    <Save className="mr-1 h-4 w-4" /> Save changes
                  </Button>
                </div>
              </form>

              <div className="border-t border-border pt-6">
                <p className="mb-2 text-xs text-muted-foreground">
                  <code className="rounded bg-muted px-1.5 py-0.5">DELETE /api/v1/{segment}/me</code>
                </p>
                <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm" className="text-destructive hover:bg-destructive/10">
                      <Trash2 className="mr-1 h-4 w-4" /> Delete account
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Delete account</DialogTitle>
                    </DialogHeader>
                    <p className="text-sm text-muted-foreground">
                      This action cannot be undone. Your profile will be removed and you will be signed out.
                    </p>
                    <div className="flex gap-2 justify-end mt-4">
                      <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
                        Cancel
                      </Button>
                      <Button variant="destructive" onClick={handleDeleteAccount}>
                        Delete
                      </Button>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="attending">
            <div className="space-y-4">
              <p className="text-xs text-muted-foreground">
                <code className="rounded bg-muted px-1.5 py-0.5">GET /api/v1/users/me/events-attending</code>
              </p>
              {eventsAttending.length === 0 ? (
                <div className="py-16 text-center text-muted-foreground">No events you're attending</div>
              ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {eventsAttending.map((e) => (
                    <EventCard key={e.id} event={e} />
                  ))}
                </div>
              )}
            </div>
          </TabsContent>

          <TabsContent value="created">
            <div className="space-y-4">
              <p className="text-xs text-muted-foreground">
                <code className="rounded bg-muted px-1.5 py-0.5">GET /api/v1/users/me/events-created</code>
              </p>
              {eventsCreated.length === 0 ? (
                <div className="py-16 text-center text-muted-foreground">No events created yet</div>
              ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {eventsCreated.map((e) => (
                    <EventCard key={e.id} event={e} />
                  ))}
                </div>
              )}
            </div>
          </TabsContent>

          <TabsContent value="history">
            <div className="space-y-4">
              <div className="flex gap-2 flex-wrap">
                <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">
                  GET /api/v1/users/me/events/history
                </code>
                <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">GET /api/tickets/my-tickets</code>
              </div>
              {eventsHistory.length === 0 ? (
                <div className="py-16 text-center text-muted-foreground">No event history</div>
              ) : (
                <div className="space-y-3">
                  {eventsHistory.map((e) => (
                    <Link key={e.id} to={`/event/${e.id}`} className="block">
                      <div className="flex items-center gap-4 rounded-xl border border-border bg-card p-4 card-shadow hover:card-shadow-hover transition-all">
                        <img src={e.image} alt={e.title} className="h-16 w-16 rounded-lg object-cover" />
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-card-foreground truncate">{e.title}</p>
                          <div className="flex items-center gap-3 mt-1 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Calendar className="h-3.5 w-3.5" /> {e.date}
                            </span>
                            <span className="flex items-center gap-1">
                              <MapPin className="h-3.5 w-3.5" /> {e.city}
                            </span>
                          </div>
                        </div>
                        {e.ticketStatus ? (
                          <Badge
                            className={
                              e.ticketStatus === "confirmed"
                                ? "bg-success/10 text-success border-0"
                                : e.ticketStatus === "refunded"
                                  ? "bg-muted text-muted-foreground border-0"
                                  : "bg-warning/10 text-warning border-0"
                            }
                          >
                            {e.ticketStatus}
                          </Badge>
                        ) : null}
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </TabsContent>
        </Tabs>
      </main>
      <Footer />
    </div>
  );
};

export default Profile;
