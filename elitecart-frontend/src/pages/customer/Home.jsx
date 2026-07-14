import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { HiOutlineArrowRight, HiOutlineTruck, HiOutlineShieldCheck, HiOutlineRefresh } from 'react-icons/hi'
import { productApi } from '../../api/productApi.js'
import ProductCard from '../../components/product/ProductCard.jsx'
import { ProductGridSkeleton } from '../../components/common/LoadingSkeleton.jsx'

const perks = [
  { icon: HiOutlineTruck, label: 'Free shipping over $100' },
  { icon: HiOutlineShieldCheck, label: 'Secure checkout' },
  { icon: HiOutlineRefresh, label: '30-day easy returns' },
]

function ProductRow({ title, products, loading }) {
  if (!loading && products.length === 0) return null
  return (
    <section className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
      <div className="mb-6 flex items-end justify-between">
        <h2 className="font-display text-2xl font-bold text-ink">{title}</h2>
        <Link to="/products" className="flex items-center gap-1 text-sm font-medium text-brand-500 hover:underline">
          View all <HiOutlineArrowRight className="h-4 w-4" />
        </Link>
      </div>
      {loading ? (
        <ProductGridSkeleton count={4} />
      ) : (
        <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-4">
          {products.slice(0, 8).map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </section>
  )
}

export default function Home() {
  const [featured, setFeatured] = useState([])
  const [latest, setLatest] = useState([])
  const [bestSellers, setBestSellers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([productApi.getFeatured(), productApi.getLatest(), productApi.getBestSellers()])
      .then(([f, l, b]) => {
        setFeatured(f.data.data)
        setLatest(l.data.data)
        setBestSellers(b.data.data)
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-mesh animate-aurora" />
        <div className="relative mx-auto max-w-7xl px-4 py-24 sm:px-6 sm:py-32 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="max-w-2xl"
          >
            <span className="badge bg-brand-50 text-brand-600">New season, curated drops</span>
            <h1 className="mt-5 font-display text-5xl font-bold leading-[1.05] text-ink sm:text-6xl">
              Shop premium.
              <br />
              <span className="bg-brand-gradient bg-clip-text text-transparent">Ship fast.</span>
            </h1>
            <p className="mt-5 max-w-lg text-lg text-ink-soft">
              EliteCart brings together considered products, honest pricing, and a checkout
              that gets out of your way.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/products" className="btn-primary">
                Start shopping <HiOutlineArrowRight className="h-4 w-4" />
              </Link>
              <Link to="/products?featured=true" className="btn-secondary">Explore featured</Link>
            </div>
          </motion.div>

          <div className="mt-14 flex flex-wrap gap-6">
            {perks.map(({ icon: Icon, label }) => (
              <div key={label} className="flex items-center gap-2 text-sm text-ink-soft">
                <Icon className="h-5 w-5 text-brand-500" />
                {label}
              </div>
            ))}
          </div>
        </div>
      </section>

      <ProductRow title="Featured picks" products={featured} loading={loading} />
      <ProductRow title="New arrivals" products={latest} loading={loading} />
      <ProductRow title="Best sellers" products={bestSellers} loading={loading} />
    </div>
  )
}
