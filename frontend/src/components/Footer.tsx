import { Link } from "react-router-dom";
import sharkFinLogo from "@/assets/shark-fin-logo.png";

const Footer = () => (
  <footer className="border-t border-navy/10 bg-navy text-navy-foreground py-12">
    <div className="container mx-auto px-4">
      <div className="grid gap-8 md:grid-cols-4">
        <div>
          <div className="mb-3 flex items-center gap-2">
            <img src={sharkFinLogo} alt="Sharks" className="h-7 w-7 brightness-0 invert" />
            <span className="text-lg font-bold text-navy-foreground">Sharks</span>
          </div>
          <p className="text-sm text-navy-foreground/70">
            Discover, create, and attend amazing events in your community.
          </p>
        </div>
        <div>
          <h4 className="mb-3 text-sm font-semibold text-navy-foreground">Platform</h4>
          <div className="flex flex-col gap-2 text-sm text-navy-foreground/70">
            <Link to="/" className="hover:text-navy-foreground transition-colors">Browse Events</Link>
            <Link to="/create" className="hover:text-navy-foreground transition-colors">Create Event</Link>
            <span className="cursor-default">Pricing</span>
          </div>
        </div>
        <div>
          <h4 className="mb-3 text-sm font-semibold text-navy-foreground">Resources</h4>
          <div className="flex flex-col gap-2 text-sm text-navy-foreground/70">
            <span className="cursor-default">Help Center</span>
            <span className="cursor-default">Community</span>
            <span className="cursor-default">Blog</span>
          </div>
        </div>
        <div>
          <h4 className="mb-3 text-sm font-semibold text-navy-foreground">Company</h4>
          <div className="flex flex-col gap-2 text-sm text-navy-foreground/70">
            <span className="cursor-default">About</span>
            <span className="cursor-default">Contact</span>
            <span className="cursor-default">Privacy Policy</span>
            <span className="cursor-default">Terms of Service</span>
          </div>
        </div>
      </div>
      <div className="mt-10 border-t border-navy-foreground/10 pt-6 text-center text-sm text-navy-foreground/60">
        © 2026 Sharks. All rights reserved.
      </div>
    </div>
  </footer>
);

export default Footer;
