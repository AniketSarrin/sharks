import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { organizationsApi } from "@/lib/api";
import type { Organization } from "@/lib/api-types";
import { ArrowLeft, Globe, Users, Calendar, Building2 } from "lucide-react";

const OrganizationDetail = () => {
  const { id } = useParams();
  const [org, setOrg] = useState<Organization | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      organizationsApi.getById(id).then((res) => {
        if (res.success && res.data) setOrg(res.data as Organization);
        setLoading(false);
      });
    }
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto py-20 text-center text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (!org) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto py-20 text-center">
          <p className="text-muted-foreground">Organization not found</p>
          <Link to="/organizations"><Button variant="outline" className="mt-4">Back</Button></Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto max-w-3xl px-4 py-12">
        <Link to="/organizations" className="mb-6 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to organizations
        </Link>

        <div className="rounded-xl border border-border bg-card p-8 card-shadow">
          <p className="mb-4 text-xs text-muted-foreground">
            <code className="rounded bg-muted px-1.5 py-0.5">GET /api/organizations/{id}</code>
          </p>
          <div className="flex items-start gap-5">
            <img src={org.logo} alt={org.name} className="h-20 w-20 rounded-xl" />
            <div>
              <h1 className="text-2xl font-extrabold text-card-foreground">{org.name}</h1>
              <p className="mt-2 text-muted-foreground">{org.description}</p>
            </div>
          </div>

          <div className="mt-6 grid gap-4 border-t border-border pt-6 sm:grid-cols-3">
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="h-4 w-4 text-primary" />
              <span className="text-muted-foreground">{org.eventsCount} events hosted</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Users className="h-4 w-4 text-primary" />
              <span className="text-muted-foreground">{org.membersCount} members</span>
            </div>
            {org.website && (
              <div className="flex items-center gap-2 text-sm">
                <Globe className="h-4 w-4 text-primary" />
                <a href={org.website} target="_blank" rel="noopener noreferrer" className="text-primary hover:underline truncate">
                  {org.website.replace("https://", "")}
                </a>
              </div>
            )}
          </div>

          <div className="mt-6 border-t border-border pt-6">
            <p className="text-sm text-muted-foreground">
              Created {new Date(org.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
            </p>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default OrganizationDetail;
