import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Menu, X } from "lucide-react";
import sharkFinLogo from "@/assets/shark-fin-logo.png";
import { getStoredRole } from "@/lib/auth-storage";

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const isAdmin = getStoredRole() === "admin";

  const navLinks = [
    { to: "/", label: "Discover" },
    { to: "/create", label: "Create Event" },
    { to: "/my-tickets", label: "My Tickets" },
    { to: "/organizations", label: "Organizations" },
    { to: "/profile", label: "Profile" },
    ...(isAdmin ? [{ to: "/admin", label: "Admin" }] : []),
  ];

  return (
    <nav className="sticky top-0 z-50 border-b border-navy/20 bg-navy text-navy-foreground nav-blur">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2">
          <img src={sharkFinLogo} alt="Sharks" className="h-8 w-8 brightness-0 invert" />
          <span className="text-xl font-bold text-navy-foreground">Sharks</span>
        </Link>

        {/* Desktop nav */}
        <div className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                location.pathname === link.to
                  ? "bg-navy-foreground/15 text-navy-foreground"
                  : "text-navy-foreground/70 hover:bg-navy-foreground/10 hover:text-navy-foreground"
              }`}
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className="hidden items-center gap-2 md:flex">
          <Link to="/login">
            <Button variant="ghost" size="sm">
              Log in
            </Button>
          </Link>
          <Link to="/signup">
            <Button size="sm">Sign up</Button>
          </Link>
        </div>

        {/* Mobile toggle */}
        <button
          className="rounded-lg p-2 text-navy-foreground/70 hover:bg-navy-foreground/10 md:hidden"
          onClick={() => setIsOpen(!isOpen)}
        >
          {isOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      {/* Mobile menu */}
      {isOpen && (
        <div className="border-t border-navy-foreground/10 bg-navy px-4 pb-4 pt-2 md:hidden animate-fade-in">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              onClick={() => setIsOpen(false)}
              className="block rounded-lg px-4 py-2.5 text-sm font-medium text-navy-foreground/70 hover:bg-navy-foreground/10 hover:text-navy-foreground"
            >
              {link.label}
            </Link>
          ))}
          <div className="mt-3 flex gap-2 border-t border-navy-foreground/10 pt-3">
            <Link to="/login" className="flex-1" onClick={() => setIsOpen(false)}>
              <Button variant="outline" size="sm" className="w-full">Log in</Button>
            </Link>
            <Link to="/signup" className="flex-1" onClick={() => setIsOpen(false)}>
              <Button size="sm" className="w-full">Sign up</Button>
            </Link>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
