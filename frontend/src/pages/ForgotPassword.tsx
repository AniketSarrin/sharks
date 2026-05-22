import { useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "@/components/NavBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authApi } from "@/lib/api";
import { ArrowLeft, Mail } from "lucide-react";
import { toast } from "sonner";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) { toast.error("Please enter your email"); return; }
    const res = await authApi.forgotPassword({ email });
    if (res.success) {
      setSent(true);
      toast.success(res.message);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="container mx-auto flex min-h-[calc(100vh-4rem)] items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="rounded-xl border border-border bg-card p-8 card-shadow">
            <p className="mb-4 text-xs text-muted-foreground">
              <code className="rounded bg-muted px-1.5 py-0.5">POST /api/v1/auth/forgot-password</code>
            </p>
            <div className="mb-6 text-center">
              <h1 className="text-2xl font-extrabold text-card-foreground">Forgot Password</h1>
              <p className="mt-1 text-sm text-muted-foreground">
                {sent ? "Check your email for a reset link" : "Enter your email to receive a reset link"}
              </p>
            </div>
            {sent ? (
              <div className="space-y-4 text-center">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
                  <Mail className="h-8 w-8 text-success" />
                </div>
                <p className="text-sm text-muted-foreground">
                  We've sent a password reset link to <strong className="text-card-foreground">{email}</strong>
                </p>
                <Button variant="outline" className="w-full" onClick={() => setSent(false)}>Send again</Button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <Label htmlFor="email">Email</Label>
                  <Input id="email" type="email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} className="mt-1.5" />
                </div>
                <Button type="submit" className="w-full" size="lg">Send Reset Link</Button>
              </form>
            )}
            <div className="mt-6 text-center">
              <Link to="/login" className="inline-flex items-center gap-1 text-sm text-primary hover:underline">
                <ArrowLeft className="h-3.5 w-3.5" /> Back to login
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
