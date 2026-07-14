import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { HiOutlineTrash, HiOutlineMinus, HiOutlinePlus, HiOutlineShoppingBag, HiOutlineArrowRight } from 'react-icons/hi'
import { fetchCart, updateCartItem, removeCartItem } from '../../features/cart/cartSlice.js'
import EmptyState from '../../components/common/EmptyState.jsx'

export default function Cart() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { cart, status } = useSelector((state) => state.cart)

  useEffect(() => {
    dispatch(fetchCart())
  }, [dispatch])

  if (status === 'loading' && !cart) {
    return <div className="mx-auto max-w-5xl px-4 py-24 text-center text-ink-faint">Loading cart…</div>
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-10">
        <EmptyState
          icon={HiOutlineShoppingBag}
          title="Your cart is empty"
          description="Looks like you haven't added anything yet. Let's fix that."
          action={<Link to="/products" className="btn-primary">Start shopping</Link>}
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold text-ink">Your cart</h1>

      <div className="mt-8 grid grid-cols-1 gap-10 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <div key={item.id} className="card flex gap-4 p-4">
              <img
                src={item.productImageUrl || 'https://placehold.co/120x120?text=EliteCart'}
                alt={item.productName}
                className="h-24 w-24 rounded-xl object-cover"
              />
              <div className="flex flex-1 flex-col justify-between">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-ink">{item.productName}</p>
                    <p className="mt-1 font-mono text-sm text-ink-faint">${item.unitPrice.toFixed(2)} each</p>
                  </div>
                  <button
                    onClick={() => dispatch(removeCartItem(item.id))}
                    className="text-ink-faint hover:text-accent-rose"
                    aria-label="Remove item"
                  >
                    <HiOutlineTrash className="h-4 w-4" />
                  </button>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center rounded-full border border-ink/10">
                    <button
                      onClick={() => dispatch(updateCartItem({ itemId: item.id, quantity: Math.max(1, item.quantity - 1) }))}
                      className="p-2 text-ink-soft"
                      aria-label="Decrease quantity"
                    >
                      <HiOutlineMinus className="h-3.5 w-3.5" />
                    </button>
                    <span className="w-6 text-center text-sm font-medium">{item.quantity}</span>
                    <button
                      onClick={() => dispatch(updateCartItem({ itemId: item.id, quantity: Math.min(item.availableStock, item.quantity + 1) }))}
                      className="p-2 text-ink-soft"
                      aria-label="Increase quantity"
                    >
                      <HiOutlinePlus className="h-3.5 w-3.5" />
                    </button>
                  </div>
                  <p className="font-mono text-sm font-semibold text-ink">${item.lineTotal.toFixed(2)}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="card h-fit space-y-3 p-6">
          <h2 className="font-display font-semibold text-ink">Order summary</h2>
          <div className="flex justify-between text-sm text-ink-soft">
            <span>Subtotal</span><span className="font-mono">${cart.subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm text-ink-soft">
            <span>Tax</span><span className="font-mono">${cart.tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm text-ink-soft">
            <span>Shipping</span>
            <span className="font-mono">{cart.shipping === 0 ? 'Free' : `$${cart.shipping.toFixed(2)}`}</span>
          </div>
          <div className="border-t border-ink/5 pt-3 flex justify-between font-semibold text-ink">
            <span>Total</span><span className="font-mono">${cart.grandTotal.toFixed(2)}</span>
          </div>
          <button onClick={() => navigate('/checkout')} className="btn-primary w-full mt-2">
            Checkout <HiOutlineArrowRight className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  )
}
