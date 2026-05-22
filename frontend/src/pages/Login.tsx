import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "@/components/NavBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authApi } from "@/lib/api";
import { toast } from "sonner";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const res = await authApi.login({ email, password });
    if (res.success) {
      toast.success("Logged in successfully!");
      navigate("/profile");
    } else {
      toast.error(res.error || "Login failed");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="container mx-auto flex min-h-[calc(100vh-4rem)] items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="rounded-xl border border-border bg-card p-8 card-shadow">
            <p className="mb-4 text-xs text-muted-foreground">
              <code className="rounded bg-muted px-1.5 py-0.5">POST /api/v1/auth/login</code>
            </p>
            <div className="mb-6 text-center">
              <h1 className="text-2xl font-extrabold text-card-foreground">Welcome back</h1>
              <p className="mt-1 text-sm text-muted-foreground">Log in to manage your events</p>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} className="mt-1.5" />
              </div>
              <div>
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">Password</Label>
                  <Link to="/forgot-password" className="text-xs text-primary hover:underline">Forgot password?</Link>
                </div>
                <Input id="password" type="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} className="mt-1.5" />
              </div>
              <Button type="submit" className="w-full" size="lg">Log in</Button>
            </form>
            <p className="mt-6 text-center text-sm text-muted-foreground">
              Don't have an account?{" "}
              <Link to="/signup" className="font-medium text-primary hover:underline">Sign up</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
