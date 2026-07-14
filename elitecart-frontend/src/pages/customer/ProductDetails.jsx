import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { HiOutlineHeart, HiOutlineShoppingBag, HiOutlineMinus, HiOutlinePlus, HiOutlineTruck, HiOutlineRefresh } from 'react-icons/hi'
import { productApi } from '../../api/productApi.js'
import { addToCart } from '../../features/cart/cartSlice.js'
import { addToWishlist } from '../../features/wishlist/wishlistSlice.js'
import StarRating from '../../components/common/StarRating.jsx'

export default function ProductDetails() {
  const { slug } = useParams()
  const dispatch = useDispatch()
  const { user } = useSelector((state) => state.auth)
  const [product, setProduct] = useState(null)
  const [activeImage, setActiveImage] = useState(0)
  const [quantity, setQuantity] = useState(1)
  const [reviews, setReviews] = useState(null)
  const [loading, setLoading] = useState(true)

  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  useEffect(() => {
    setLoading(true)
    productApi
      .getBySlug(slug)
      .then((res) => {
        setProduct(res.data.data)
        return productApi.getReviews(res.data.data.id, { page: 0, size: 10 })
      })
      .then((res) => setReviews(res.data.data))
      .finally(() => setLoading(false))
  }, [slug])

  const handleAddToCart = () => {
    dispatch(addToCart({ productId: product.id, quantity }))
  }

  const handleAddToWishlist = () => {
    dispatch(addToWishlist(product.id))
  }

  const submitReview = async (formData) => {
    try {
      await productApi.addReview(product.id, { rating: Number(formData.rating), comment: formData.comment })
      toast.success('Thanks for your review!')
      reset()
      const res = await productApi.getReviews(product.id, { page: 0, size: 10 })
      setReviews(res.data.data)
      const updated = await productApi.getBySlug(slug)
      setProduct(updated.data.data)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not submit review')
    }
  }

  if (loading) {
    return <div className="mx-auto max-w-7xl px-4 py-24 text-center text-ink-faint">Loading product…</div>
  }

  if (!product) {
    return <div className="mx-auto max-w-7xl px-4 py-24 text-center text-ink-faint">Product not found.</div>
  }

  const hasDiscount = product.discountPrice && product.discountPrice < product.price
  const images = product.images?.length ? product.images : [{ imageUrl: 'https://placehold.co/600x600?text=EliteCart' }]

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <nav className="mb-6 text-sm text-ink-faint">
        <Link to="/products" className="hover:text-ink">Shop</Link> / <span className="text-ink">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 gap-10 lg:grid-cols-2">
        {/* Gallery */}
        <div>
          <div className="aspect-square overflow-hidden rounded-3xl bg-ink/5">
            <img src={images[activeImage].imageUrl} alt={product.name} className="h-full w-full object-cover" />
          </div>
          {images.length > 1 && (
            <div className="mt-3 flex gap-2">
              {images.map((img, idx) => (
                <button
                  key={img.id || idx}
                  onClick={() => setActiveImage(idx)}
                  className={`h-16 w-16 overflow-hidden rounded-xl border-2 ${activeImage === idx ? 'border-brand-500' : 'border-transparent'}`}
                >
                  <img src={img.imageUrl} alt="" className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Details */}
        <div>
          <p className="text-sm font-medium text-brand-500">{product.categoryName}</p>
          <h1 className="mt-1 font-display text-3xl font-bold text-ink">{product.name}</h1>
          <div className="mt-3">
            <StarRating rating={product.rating} reviewCount={product.reviewCount} showValue />
          </div>

          <div className="mt-5 flex items-baseline gap-3 font-mono">
            <span className="text-3xl font-bold text-ink">${product.effectivePrice?.toFixed(2)}</span>
            {hasDiscount && <span className="text-lg text-ink-faint line-through">${product.price?.toFixed(2)}</span>}
          </div>

          <p className="mt-5 text-sm leading-relaxed text-ink-soft">{product.description}</p>

          <div className="mt-4 flex items-center gap-2 text-sm">
            <span className={`h-2 w-2 rounded-full ${product.inStock ? 'bg-accent-emerald' : 'bg-accent-rose'}`} />
            {product.inStock ? `In stock (${product.stockQuantity} available)` : 'Out of stock'}
          </div>

          <div className="mt-6 flex items-center gap-4">
            <div className="flex items-center rounded-full border border-ink/10">
              <button onClick={() => setQuantity((q) => Math.max(1, q - 1))} className="p-3 text-ink-soft" aria-label="Decrease quantity">
                <HiOutlineMinus className="h-4 w-4" />
              </button>
              <span className="w-8 text-center font-medium">{quantity}</span>
              <button onClick={() => setQuantity((q) => Math.min(product.stockQuantity, q + 1))} className="p-3 text-ink-soft" aria-label="Increase quantity">
                <HiOutlinePlus className="h-4 w-4" />
              </button>
            </div>

            <button onClick={handleAddToCart} disabled={!product.inStock} className="btn-primary flex-1">
              <HiOutlineShoppingBag className="h-4 w-4" /> Add to cart
            </button>
            <button onClick={handleAddToWishlist} className="btn-secondary" aria-label="Add to wishlist">
              <HiOutlineHeart className="h-4 w-4" />
            </button>
          </div>

          <div className="mt-8 space-y-3 border-t border-ink/5 pt-6 text-sm text-ink-soft">
            <div className="flex items-center gap-2"><HiOutlineTruck className="h-4 w-4 text-brand-500" /> Free shipping on orders over $100</div>
            <div className="flex items-center gap-2"><HiOutlineRefresh className="h-4 w-4 text-brand-500" /> 30-day hassle-free returns</div>
          </div>
        </div>
      </div>

      {/* Reviews */}
      <section className="mt-16 border-t border-ink/5 pt-10">
        <h2 className="font-display text-2xl font-bold text-ink">Customer reviews</h2>

        {user && (
          <form onSubmit={handleSubmit(submitReview)} className="card mt-6 max-w-lg space-y-3 p-5">
            <div>
              <label className="label">Rating</label>
              <select className="input-field" {...register('rating', { required: true })}>
                {[5, 4, 3, 2, 1].map((r) => <option key={r} value={r}>{r} star{r > 1 ? 's' : ''}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Comment</label>
              <textarea className="input-field" rows={3} placeholder="Share your experience…" {...register('comment')} />
            </div>
            <button type="submit" className="btn-primary">Submit review</button>
          </form>
        )}

        <div className="mt-8 space-y-6">
          {reviews?.content?.length ? (
            reviews.content.map((review) => (
              <div key={review.id} className="border-b border-ink/5 pb-6">
                <div className="flex items-center justify-between">
                  <p className="font-medium text-ink">{review.userName}</p>
                  <StarRating rating={review.rating} />
                </div>
                {review.comment && <p className="mt-2 text-sm text-ink-soft">{review.comment}</p>}
              </div>
            ))
          ) : (
            <p className="text-sm text-ink-faint">No reviews yet — be the first to share your thoughts.</p>
          )}
        </div>
      </section>
    </div>
  )
}
