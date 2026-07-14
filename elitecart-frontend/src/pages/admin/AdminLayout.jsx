import { NavLink, Outlet, Link } from 'react-router-dom'
import { useSelector } from 'react-redux'
import {
  HiOutlineViewGrid, HiOutlineCube, HiOutlineTag, HiOutlineShoppingBag,
  HiOutlineUsers, HiOutlineArrowLeft,
} from 'react-icons/hi'

const navItems = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: HiOutlineViewGrid },
  { to: '/admin/products', label: 'Products', icon: HiOutlineCube },
  { to: '/admin/categories', label: 'Categories', icon: HiOutlineTag },
  { to: '/admin/orders', label: 'Orders', icon: HiOutlineShoppingBag },
  { to: '/admin/customers', label: 'Customers', icon: HiOutlineUsers },
]

export default function AdminLayout() {
  const { user } = useSelector((state) => state.auth)

  return (
    <div className="flex min-h-screen bg-canvas">
      <aside className="hidden w-64 shrink-0 border-r border-ink/5 bg-white lg:block">
        <div className="p-6">
          <Link to="/" className="font-display text-xl font-bold bg-brand-gradient bg-clip-text text-transparent">
            EliteCart
          </Link>
          <p className="mt-0.5 text-xs text-ink-faint">Admin console</p>
        </div>
        <nav className="space-y-1 px-3">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive ? 'bg-brand-50 text-brand-600' : 'text-ink-soft hover:bg-ink/5'
                }`
              }
            >
              <Icon className="h-4.5 w-4.5" /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="absolute bottom-0 w-64 border-t border-ink/5 p-4">
          <Link to="/" className="flex items-center gap-2 text-sm text-ink-faint hover:text-ink">
            <HiOutlineArrowLeft className="h-4 w-4" /> Back to store
          </Link>
        </div>
      </aside>

      <div className="flex-1">
        <header className="flex h-16 items-center justify-between border-b border-ink/5 bg-white px-6 lg:hidden">
          <span className="font-display font-bold bg-brand-gradient bg-clip-text text-transparent">EliteCart Admin</span>
        </header>
        <div className="p-6 lg:p-10">
          <div className="mb-6 flex items-center justify-between lg:hidden">
            <nav className="flex gap-2 overflow-x-auto">
              {navItems.map(({ to, label }) => (
                <NavLink key={to} to={to} className={({ isActive }) => `whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-medium ${isActive ? 'bg-brand-gradient text-white' : 'bg-ink/5 text-ink-soft'}`}>
                  {label}
                </NavLink>
              ))}
            </nav>
          </div>
          <p className="mb-6 text-sm text-ink-faint">Welcome back, <span className="font-medium text-ink">{user?.firstName}</span></p>
          <Outlet />
        </div>
      </div>
    </div>
  )
}
