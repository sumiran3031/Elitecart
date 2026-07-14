import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'

/**
 * Guards customer/admin routes. Pass requireAdmin to additionally require
 * the ROLE_ADMIN authority (used for /admin/** routes).
 */
export default function ProtectedRoute({ requireAdmin = false }) {
  const { user } = useSelector((state) => state.auth)
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requireAdmin && !user.roles?.includes('ROLE_ADMIN')) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
