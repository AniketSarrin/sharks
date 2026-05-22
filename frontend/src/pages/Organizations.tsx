import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { organizationsApi } from "@/lib/api";
import type { Organization } from "@/lib/api-types";
import { Building2, Globe, Users, Calendar, Plus, ExternalLink } from "lucide-react";
import { toast } from "sonner";

const Organizations = () => {
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState({ name: "", description: "", website: "" });

  useEffect(() => {
    organizationsApi.getAll().then((res) => {
      if (res.success) setOrgs(res.data);
      setLoading(false);
    });
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name) { toast.error("Name is required"); return; }
    const res = await organizationsApi.create(form);
    if (res.success) {
      toast.success("Organization created!");
      setDialogOpen(false);
      setForm({ name: "", description: "", website: "" });
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto max-w-5xl px-4 py-12">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-extrabold text-foreground">Organizations</h1>
            <p className="mt-1 text-muted-foreground">
              <code className="rounded bg-muted px-1.5 py-0.5 text-xs">GET /api/organizations</code>
            </p>
          </div>
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger asChild>
              <Button><Plus className="mr-1 h-4 w-4" /> Create Organization</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Organization</DialogTitle>
              </DialogHeader>
              <p className="text-xs text-muted-foreground mb-3">
                <code className="rounded bg-muted px-1.5 py-0.5">POST /api/organizations</code>
              </p>
              <form onSubmit={handleCreate} className="space-y-4">
                <div>
                  <Label htmlFor="orgName">Name *</Label>
                  <Input id="orgName" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} className="mt-1.5" placeholder="Organization name" />
                </div>
                <div>
                  <Label htmlFor="orgDesc">Description</Label>
                  <Textarea id="orgDesc" value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} className="mt-1.5" rows={3} placeholder="What does your organization do?" />
                </div>
                <div>
                  <Label htmlFor="orgWeb">Website</Label>
                  <Input id="orgWeb" value={form.website} onChange={(e) => setForm((f) => ({ ...f, website: e.target.value }))} className="mt-1.5" placeholder="https://..." />
                </div>
                <Button type="submit" className="w-full">Create</Button>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        {loading ? (
          <div className="py-20 text-center text-muted-foreground">Loading organizations...</div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2">
            {orgs.map((org) => (
              <Link to={`/organizations/${org.id}`} key={org.id} className="group">
                <div className="rounded-xl border border-border bg-card p-6 card-shadow transition-all hover:card-shadow-hover hover:-translate-y-1">
                  <div className="flex items-start gap-4">
                    <img src={org.logo} alt={org.name} className="h-14 w-14 rounded-xl" />
                    <div className="flex-1">
                      <h3 className="text-lg font-bold text-card-foreground group-hover:text-primary transition-colors">{org.name}</h3>
                      <p className="mt-1 text-sm text-muted-foreground line-clamp-2">{org.description}</p>
                    </div>
                  </div>
                  <div className="mt-4 flex gap-4 text-sm text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <Calendar className="h-3.5 w-3.5" /> {org.eventsCount} events
                    </div>
                    <div className="flex items-center gap-1">
                      <Users className="h-3.5 w-3.5" /> {org.membersCount} members
                    </div>
                    {org.website && (
                      <div className="flex items-center gap-1">
                        <Globe className="h-3.5 w-3.5" /> Website
                      </div>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};

export default Organizations;
