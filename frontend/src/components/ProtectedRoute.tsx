import { Navigate, useLocation } from "react-router-dom";
import type { BackendUserRole } from "@/lib/api-types";
import { getAccessToken, getStoredRole } from "@/lib/auth-storage";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: BackendUserRole;
}

const ProtectedRoute = ({ children, requiredRole }: ProtectedRouteProps) => {
  const location = useLocation();

  if (!getAccessToken()) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && getStoredRole() !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
