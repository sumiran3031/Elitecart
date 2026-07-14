import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { HiOutlineShoppingBag, HiOutlineHeart, HiOutlineUser, HiOutlineMenu, HiOutlineX, HiOutlineSearch } from 'react-icons/hi'
import { logoutUser } from '../../features/auth/authSlice.js'

const navLinks = [
  { to: '/products', label: 'Shop' },
  { to: '/products?featured=true', label: 'Featured' },
  { to: '/products?sort=latest', label: 'New Arrivals' },
]

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { user } = useSelector((state) => state.auth)
  const { cart } = useSelector((state) => state.cart)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const itemCount = cart?.totalItems || 0

  const handleLogout = async () => {
    await dispatch(logoutUser())
    navigate('/')
  }

  return (
    <header className="sticky top-0 z-50 glass-nav">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-display text-xl font-bold">
            <span className="bg-brand-gradient bg-clip-text text-transparent">EliteCart</span>
          </Link>

          <nav className="hidden md:flex items-center gap-1">
            {navLinks.map((link) => (
              <NavLink
                key={link.label}
                to={link.to}
                className="btn-ghost text-sm"
              >
                {link.label}
              </NavLink>
            ))}
          </nav>

          <div className="hidden md:flex items-center gap-2">
            <Link to="/products" className="btn-ghost" aria-label="Search products">
              <HiOutlineSearch className="h-5 w-5" />
            </Link>
            <Link to="/wishlist" className="btn-ghost" aria-label="Wishlist">
              <HiOutlineHeart className="h-5 w-5" />
            </Link>
            <Link to="/cart" className="btn-ghost relative" aria-label="Cart">
              <HiOutlineShoppingBag className="h-5 w-5" />
              {itemCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-accent-rose text-[10px] font-semibold text-white">
                  {itemCount > 9 ? '9+' : itemCount}
                </span>
              )}
            </Link>

            {user ? (
              <div className="group relative">
                <button className="btn-ghost">
                  <HiOutlineUser className="h-5 w-5" />
                  <span className="text-sm">{user.firstName}</span>
                </button>
                <div className="invisible absolute right-0 mt-1 w-48 rounded-xl border border-ink/5 bg-white py-1.5 opacity-0 shadow-card transition-all group-hover:visible group-hover:opacity-100">
                  <Link to="/profile" className="block px-4 py-2 text-sm text-ink-soft hover:bg-ink/5 hover:text-ink">My Profile</Link>
                  <Link to="/orders" className="block px-4 py-2 text-sm text-ink-soft hover:bg-ink/5 hover:text-ink">My Orders</Link>
                  {user.roles?.includes('ROLE_ADMIN') && (
                    <Link to="/admin/dashboard" className="block px-4 py-2 text-sm text-ink-soft hover:bg-ink/5 hover:text-ink">Admin Dashboard</Link>
                  )}
                  <button onClick={handleLogout} className="block w-full px-4 py-2 text-left text-sm text-accent-rose hover:bg-ink/5">
                    Log out
                  </button>
                </div>
              </div>
            ) : (
              <Link to="/login" className="btn-primary text-sm px-5 py-2.5">Sign in</Link>
            )}
          </div>

          <button className="md:hidden btn-ghost" onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu">
            {menuOpen ? <HiOutlineX className="h-6 w-6" /> : <HiOutlineMenu className="h-6 w-6" />}
          </button>
        </div>

        {menuOpen && (
          <div className="md:hidden border-t border-ink/5 py-3 space-y-1">
            {navLinks.map((link) => (
              <Link key={link.label} to={link.to} className="block px-2 py-2 text-sm text-ink-soft" onClick={() => setMenuOpen(false)}>
                {link.label}
              </Link>
            ))}
            <Link to="/cart" className="block px-2 py-2 text-sm text-ink-soft" onClick={() => setMenuOpen(false)}>Cart ({itemCount})</Link>
            <Link to="/wishlist" className="block px-2 py-2 text-sm text-ink-soft" onClick={() => setMenuOpen(false)}>Wishlist</Link>
            {user ? (
              <>
                <Link to="/profile" className="block px-2 py-2 text-sm text-ink-soft" onClick={() => setMenuOpen(false)}>My Profile</Link>
                <button onClick={handleLogout} className="block w-full px-2 py-2 text-left text-sm text-accent-rose">Log out</button>
              </>
            ) : (
              <Link to="/login" className="block px-2 py-2 text-sm font-medium text-brand-500" onClick={() => setMenuOpen(false)}>Sign in</Link>
            )}
          </div>
        )}
      </div>
    </header>
  )
}
