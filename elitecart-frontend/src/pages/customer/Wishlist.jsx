import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { HiOutlineHeart, HiOutlineTrash, HiOutlineShoppingBag } from 'react-icons/hi'
import { fetchWishlist, removeFromWishlist, moveWishlistItemToCart } from '../../features/wishlist/wishlistSlice.js'
import EmptyState from '../../components/common/EmptyState.jsx'

export default function Wishlist() {
  const dispatch = useDispatch()
  const { wishlist, status } = useSelector((state) => state.wishlist)

  useEffect(() => {
    dispatch(fetchWishlist())
  }, [dispatch])

  if (status === 'loading' && !wishlist) {
    return <div className="mx-auto max-w-5xl px-4 py-24 text-center text-ink-faint">Loading wishlist…</div>
  }

  if (!wishlist || wishlist.items.length === 0) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-10">
        <EmptyState
          icon={HiOutlineHeart}
          title="Your wishlist is empty"
          description="Save items you love here so you can find them later."
          action={<Link to="/products" className="btn-primary">Browse products</Link>}
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold text-ink">Your wishlist</h1>

      <div className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        {wishlist.items.map((item) => (
          <div key={item.id} className="card flex gap-4 p-4">
            <img
              src={item.productImageUrl || 'https://placehold.co/120x120?text=EliteCart'}
              alt={item.productName}
              className="h-20 w-20 rounded-xl object-cover"
            />
            <div className="flex flex-1 flex-col justify-between">
              <div>
                <p className="font-medium text-ink">{item.productName}</p>
                <p className="mt-1 font-mono text-sm text-ink-faint">${item.effectivePrice.toFixed(2)}</p>
                {!item.inStock && <p className="mt-1 text-xs text-accent-rose">Out of stock</p>}
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => dispatch(moveWishlistItemToCart(item.id))}
                  disabled={!item.inStock}
                  className="btn-ghost text-xs px-3 py-1.5 bg-ink/5"
                >
                  <HiOutlineShoppingBag className="h-3.5 w-3.5" /> Move to cart
                </button>
                <button
                  onClick={() => dispatch(removeFromWishlist(item.id))}
                  className="btn-ghost text-xs px-3 py-1.5 text-accent-rose"
                >
                  <HiOutlineTrash className="h-3.5 w-3.5" /> Remove
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
