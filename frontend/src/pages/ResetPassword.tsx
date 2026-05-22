import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Navbar from "@/components/NavBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authApi } from "@/lib/api";
import { CheckCircle } from "lucide-react";
import { toast } from "sonner";

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "mock-token";
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [done, setDone] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password.length < 6) { toast.error("Password must be at least 6 characters"); return; }
    if (password !== confirm) { toast.error("Passwords do not match"); return; }
    const res = await authApi.resetPassword({ token, newPassword: password });
    if (res.success) {
      setDone(true);
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
              <code className="rounded bg-muted px-1.5 py-0.5">POST /api/v1/auth/reset-password</code>
            </p>
            {done ? (
              <div className="space-y-4 text-center">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
                  <CheckCircle className="h-8 w-8 text-success" />
                </div>
                <h1 className="text-2xl font-extrabold text-card-foreground">Password Reset!</h1>
                <p className="text-sm text-muted-foreground">Your password has been successfully reset.</p>
                <Link to="/login"><Button className="w-full">Go to Login</Button></Link>
              </div>
            ) : (
              <>
                <div className="mb-6 text-center">
                  <h1 className="text-2xl font-extrabold text-card-foreground">Reset Password</h1>
                  <p className="mt-1 text-sm text-muted-foreground">Enter your new password</p>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <Label htmlFor="password">New Password</Label>
                    <Input id="password" type="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} className="mt-1.5" />
                  </div>
                  <div>
                    <Label htmlFor="confirm">Confirm Password</Label>
                    <Input id="confirm" type="password" placeholder="••••••••" value={confirm} onChange={(e) => setConfirm(e.target.value)} className="mt-1.5" />
                  </div>
                  <Button type="submit" className="w-full" size="lg">Reset Password</Button>
                </form>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
