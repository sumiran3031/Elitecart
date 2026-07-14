import { Link } from 'react-router-dom'
import { HiOutlineHome } from 'react-icons/hi'

export default function NotFound() {
  return (
    <div className="flex min-h-[70vh] flex-col items-center justify-center px-4 text-center">
      <span className="font-display text-7xl font-bold bg-brand-gradient bg-clip-text text-transparent">404</span>
      <h1 className="mt-4 font-display text-2xl font-bold text-ink">Page not found</h1>
      <p className="mt-2 max-w-sm text-sm text-ink-faint">
        The page you're looking for doesn't exist or may have moved.
      </p>
      <Link to="/" className="btn-primary mt-6">
        <HiOutlineHome className="h-4 w-4" /> Back to home
      </Link>
    </div>
  )
}
