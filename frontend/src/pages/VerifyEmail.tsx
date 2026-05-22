import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Navbar from "@/components/NavBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authApi } from "@/lib/api";
import { CheckCircle, Mail } from "lucide-react";
import { toast } from "sonner";

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [verified, setVerified] = useState(false);
  const [resendEmail, setResendEmail] = useState("");
  const [resent, setResent] = useState(false);

  const handleVerify = async () => {
    const res = await authApi.verifyEmail({ token: token || "mock-token" });
    if (res.success) {
      setVerified(true);
      toast.success(res.message);
    }
  };

  const handleResend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resendEmail) { toast.error("Please enter your email"); return; }
    const res = await authApi.resendVerification({ email: resendEmail });
    if (res.success) {
      setResent(true);
      toast.success(res.message);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="container mx-auto flex min-h-[calc(100vh-4rem)] items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="rounded-xl border border-border bg-card p-8 card-shadow">
            <div className="mb-4 flex gap-2 flex-wrap">
              <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">POST /api/v1/auth/verify-email</code>
              <code className="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">POST /api/v1/auth/resend-verification</code>
            </div>

            {verified ? (
              <div className="space-y-4 text-center">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
                  <CheckCircle className="h-8 w-8 text-success" />
                </div>
                <h1 className="text-2xl font-extrabold text-card-foreground">Email Verified!</h1>
                <p className="text-sm text-muted-foreground">Your email has been verified successfully.</p>
                <Link to="/"><Button className="w-full">Browse Events</Button></Link>
              </div>
            ) : token ? (
              <div className="space-y-4 text-center">
                <h1 className="text-2xl font-extrabold text-card-foreground">Verify Email</h1>
                <p className="text-sm text-muted-foreground">Click the button below to verify your email address.</p>
                <Button className="w-full" size="lg" onClick={handleVerify}>Verify My Email</Button>
              </div>
            ) : (
              <div className="space-y-6">
                <div className="text-center">
                  <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
                    <Mail className="h-8 w-8 text-primary" />
                  </div>
                  <h1 className="text-2xl font-extrabold text-card-foreground">Verify Your Email</h1>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {resent ? "Verification email sent!" : "Enter your email to receive a verification link"}
                  </p>
                </div>
                {!resent && (
                  <form onSubmit={handleResend} className="space-y-4">
                    <div>
                      <Label htmlFor="email">Email</Label>
                      <Input id="email" type="email" placeholder="you@example.com" value={resendEmail} onChange={(e) => setResendEmail(e.target.value)} className="mt-1.5" />
                    </div>
                    <Button type="submit" className="w-full" size="lg">Send Verification</Button>
                  </form>
                )}
                {resent && (
                  <Button variant="outline" className="w-full" onClick={() => setResent(false)}>Send again</Button>
                )}
              </div>
            )}

            <div className="mt-6 text-center">
              <Link to="/login" className="text-sm text-primary hover:underline">Back to login</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;
