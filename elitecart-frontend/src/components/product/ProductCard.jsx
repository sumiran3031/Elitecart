import { Link } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { motion } from 'framer-motion'
import { HiOutlineHeart, HiOutlineShoppingBag } from 'react-icons/hi'
import StarRating from '../common/StarRating.jsx'
import { addToCart } from '../../features/cart/cartSlice.js'
import { addToWishlist } from '../../features/wishlist/wishlistSlice.js'

export default function ProductCard({ product }) {
  const dispatch = useDispatch()
  const hasDiscount = product.discountPrice && product.discountPrice < product.price
  const imageUrl = product.images?.[0]?.imageUrl || 'https://placehold.co/400x400?text=EliteCart'

  const handleAddToCart = (e) => {
    e.preventDefault()
    dispatch(addToCart({ productId: product.id, quantity: 1 }))
  }

  const handleAddToWishlist = (e) => {
    e.preventDefault()
    dispatch(addToWishlist(product.id))
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-40px' }}
      transition={{ duration: 0.35 }}
    >
      <Link to={`/products/${product.slug}`} className="card card-hover group block overflow-hidden p-3">
        <div className="relative aspect-square overflow-hidden rounded-xl bg-ink/5">
          <img
            src={imageUrl}
            alt={product.name}
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />

          {hasDiscount && (
            <span className="badge absolute left-2 top-2 bg-accent-rose text-white">
              -{Math.round(100 - (product.discountPrice / product.price) * 100)}%
            </span>
          )}
          {!product.inStock && (
            <span className="badge absolute left-2 top-2 bg-ink/70 text-white">Out of stock</span>
          )}

          <button
            onClick={handleAddToWishlist}
            className="absolute right-2 top-2 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-ink-soft opacity-0 shadow-sm transition-opacity duration-200 group-hover:opacity-100 hover:text-accent-rose"
            aria-label="Add to wishlist"
          >
            <HiOutlineHeart className="h-4 w-4" />
          </button>

          <button
            onClick={handleAddToCart}
            disabled={!product.inStock}
            className="absolute bottom-2 left-2 right-2 flex items-center justify-center gap-1.5 rounded-full bg-ink/90 py-2 text-xs font-medium text-white opacity-0 shadow-sm transition-opacity duration-200 group-hover:opacity-100 disabled:pointer-events-none disabled:opacity-0"
          >
            <HiOutlineShoppingBag className="h-3.5 w-3.5" /> Quick add
          </button>
        </div>

        <div className="mt-3 space-y-1">
          <p className="truncate text-sm font-medium text-ink">{product.name}</p>
          <StarRating rating={product.rating} reviewCount={product.reviewCount} size="h-3.5 w-3.5" />
          <div className="flex items-baseline gap-2 font-mono">
            <span className="text-sm font-semibold text-ink">₹{product.effectivePrice?.toFixed(2)}</span>
            {hasDiscount && (
              <span className="text-xs text-ink-faint line-through">₹{product.price?.toFixed(2)}</span>
            )}
          </div>
        </div>
      </Link>
    </motion.div>
  )
}
