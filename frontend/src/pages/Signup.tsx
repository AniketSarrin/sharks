import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "@/components/NavBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { authApi } from "@/lib/api";
import { toast } from "sonner";

const Signup = () => {
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "attendee" });
  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name || !form.email || !form.password) {
      toast.error("Please fill in all fields");
      return;
    }
    const res = await authApi.register(form);
    if (res.success) {
      toast.success("Account created!", { description: res.message });
      navigate("/verify-email");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="container mx-auto flex min-h-[calc(100vh-4rem)] items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="rounded-xl border border-border bg-card p-8 card-shadow">
            <p className="mb-4 text-xs text-muted-foreground">
              <code className="rounded bg-muted px-1.5 py-0.5">POST /api/v1/users/register</code>
            </p>
            <div className="mb-6 text-center">
              <h1 className="text-2xl font-extrabold text-card-foreground">Create your account</h1>
              <p className="mt-1 text-sm text-muted-foreground">Join Sharks and discover amazing events</p>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="name">Full Name</Label>
                <Input id="name" placeholder="John Doe" value={form.name} onChange={(e) => update("name", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" placeholder="you@example.com" value={form.email} onChange={(e) => update("email", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label htmlFor="password">Password</Label>
                <Input id="password" type="password" placeholder="••••••••" value={form.password} onChange={(e) => update("password", e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <Label>Role</Label>
                <Select value={form.role} onValueChange={(v) => update("role", v)}>
                  <SelectTrigger className="mt-1.5"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="attendee">Attendee</SelectItem>
                    <SelectItem value="organizer">Organizer</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button type="submit" className="w-full" size="lg">Sign up</Button>
            </form>
            <p className="mt-6 text-center text-sm text-muted-foreground">
              Already have an account?{" "}
              <Link to="/login" className="font-medium text-primary hover:underline">Log in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Signup;
