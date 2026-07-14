import { Link } from 'react-router-dom'
import { HiOutlineMail } from 'react-icons/hi'

export default function Footer() {
  return (
    <footer className="mt-24 border-t border-ink/5 bg-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
          <div className="col-span-2 md:col-span-1">
            <span className="font-display text-lg font-bold bg-brand-gradient bg-clip-text text-transparent">EliteCart</span>
            <p className="mt-2 text-sm text-ink-faint">Premium products, shipped fast, backed by real people.</p>
          </div>
          <div>
            <h4 className="text-sm font-semibold text-ink">Shop</h4>
            <ul className="mt-3 space-y-2 text-sm text-ink-faint">
              <li><Link to="/products" className="hover:text-ink">All Products</Link></li>
              <li><Link to="/products?featured=true" className="hover:text-ink">Featured</Link></li>
              <li><Link to="/products?sort=latest" className="hover:text-ink">New Arrivals</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm font-semibold text-ink">Account</h4>
            <ul className="mt-3 space-y-2 text-sm text-ink-faint">
              <li><Link to="/profile" className="hover:text-ink">My Profile</Link></li>
              <li><Link to="/orders" className="hover:text-ink">My Orders</Link></li>
              <li><Link to="/wishlist" className="hover:text-ink">Wishlist</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm font-semibold text-ink">Get in touch</h4>
            <p className="mt-3 flex items-center gap-2 text-sm text-ink-faint">
              <HiOutlineMail className="h-4 w-4" /> support@elitecart.com
            </p>
          </div>
        </div>
        <div className="mt-10 border-t border-ink/5 pt-6 text-xs text-ink-faint">
          © {new Date().getFullYear()} EliteCart. Built as a portfolio project.
        </div>
      </div>
    </footer>
  )
}
