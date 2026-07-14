import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'

export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="relative min-h-screen overflow-hidden bg-canvas">
      <div className="absolute inset-0 bg-mesh animate-aurora" />
      <div className="relative flex min-h-screen items-center justify-center px-4 py-12">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-md rounded-3xl border border-ink/5 bg-white/80 p-8 shadow-glass backdrop-blur-xl"
        >
          <Link to="/" className="font-display text-xl font-bold bg-brand-gradient bg-clip-text text-transparent">
            EliteCart
          </Link>
          <h1 className="mt-6 font-display text-2xl font-bold text-ink">{title}</h1>
          {subtitle && <p className="mt-1 text-sm text-ink-faint">{subtitle}</p>}
          <div className="mt-6">{children}</div>
        </motion.div>
      </div>
    </div>
  )
}
