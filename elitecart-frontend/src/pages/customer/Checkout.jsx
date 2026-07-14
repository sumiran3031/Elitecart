import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useSelector, useDispatch } from 'react-redux'
import toast from 'react-hot-toast'
import { HiOutlinePlus, HiOutlineCreditCard } from 'react-icons/hi'
import { addressApi } from '../../api/addressApi.js'
import { orderApi } from '../../api/orderApi.js'
import { fetchCart } from '../../features/cart/cartSlice.js'

const paymentMethods = [
  { value: 'CARD', label: 'Credit / Debit Card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'NET_BANKING', label: 'Net Banking' },
  { value: 'WALLET', label: 'Wallet' },
  { value: 'COD', label: 'Cash on Delivery' },
]

export default function Checkout() {
  const { cart } = useSelector((state) => state.cart)
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [addresses, setAddresses] = useState([]);
  const [shippingId, setShippingId] = useState(null)
  const [billingId, setBillingId] = useState(null)
  const [sameAsShipping, setSameAsShipping] = useState(true)
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const [showAddForm, setShowAddForm] = useState(false)
  const [placing, setPlacing] = useState(false)
  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  const loadAddresses = () => {
    addressApi.getAll().then((res) => {
      setAddresses(res.data.data)
      const def = res.data.data.find((a) => a.isDefault) || res.data.data[0]
      if (def) setShippingId(def.id)
    })
  }

  useEffect(() => {
    loadAddresses()
  }, [])

  const submitNewAddress = async (formData) => {
    try {
      const { data } = await addressApi.create(formData)
      toast.success('Address added')
      setShowAddForm(false)
      reset()
      loadAddresses()
      setShippingId(data.data.id)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not add address')
    }
  }

  const placeOrder = async () => {
    if (!shippingId || (!sameAsShipping && !billingId)) {
      toast.error('Please select shipping and billing addresses')
      return
    }
    setPlacing(true)
    try {
      const { data } = await orderApi.checkout({
        shippingAddressId: shippingId,
        billingAddressId: sameAsShipping ? shippingId : billingId,
        paymentMethod,
      })
      toast.success('Order placed successfully!')
      dispatch(fetchCart())
      navigate(`/orders/${data.data.id}`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not place order')
    } finally {
      setPlacing(false)
    }
  }

  if (!cart || cart.items.length === 0) {
    return <div className="mx-auto max-w-3xl px-4 py-24 text-center text-ink-faint">Your cart is empty.</div>
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold text-ink">Checkout</h1>

      <div className="mt-8 grid grid-cols-1 gap-10 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-6">
          <section className="card p-6">
            <div className="flex items-center justify-between">
              <h2 className="font-display font-semibold text-ink">Shipping address</h2>
              <button onClick={() => setShowAddForm(!showAddForm)} className="btn-ghost text-xs">
                <HiOutlinePlus className="h-4 w-4" /> Add new
              </button>
            </div>

            <div className="mt-4 space-y-2">
              {addresses.map((addr) => (
                <label key={addr.id} className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3 text-sm ${shippingId === addr.id ? 'border-brand-500 bg-brand-50' : 'border-ink/10'}`}>
                  <input type="radio" name="shipping" className="mt-1" checked={shippingId === addr.id} onChange={() => setShippingId(addr.id)} />
                  <span>
                    <span className="font-medium text-ink">{addr.fullName}</span> — {addr.phoneNumber}<br />
                    {addr.addressLine1}, {addr.addressLine2 ? `${addr.addressLine2}, ` : ''}{addr.city}, {addr.state} {addr.postalCode}, {addr.country}
                  </span>
                </label>
              ))}
              {addresses.length === 0 && <p className="text-sm text-ink-faint">No addresses yet — add one to continue.</p>}
            </div>

            {showAddForm && (
              <form onSubmit={handleSubmit(submitNewAddress)} className="mt-4 grid grid-cols-2 gap-3 border-t border-ink/5 pt-4">
                <input className="input-field col-span-2" placeholder="Full name" {...register('fullName', { required: true })} />
                <input className="input-field col-span-2" placeholder="Phone number" {...register('phoneNumber', { required: true })} />
                <input className="input-field col-span-2" placeholder="Address line 1" {...register('addressLine1', { required: true })} />
                <input className="input-field col-span-2" placeholder="Address line 2 (optional)" {...register('addressLine2')} />
                <input className="input-field" placeholder="City" {...register('city', { required: true })} />
                <input className="input-field" placeholder="State" {...register('state', { required: true })} />
                <input className="input-field" placeholder="Postal code" {...register('postalCode', { required: true })} />
                <input className="input-field" placeholder="Country" {...register('country', { required: true })} />
                <select className="input-field col-span-2" {...register('addressType', { required: true })}>
                  <option value="SHIPPING">Shipping</option>
                  <option value="BILLING">Billing</option>
                </select>
                <button type="submit" className="btn-primary col-span-2">Save address</button>
              </form>
            )}
          </section>

          <section className="card p-6">
            <label className="flex items-center gap-2 text-sm text-ink-soft">
              <input type="checkbox" className="rounded border-ink/20" checked={sameAsShipping} onChange={(e) => setSameAsShipping(e.target.checked)} />
              Billing address is the same as shipping
            </label>

            {!sameAsShipping && (
              <div className="mt-4 space-y-2">
                {addresses.map((addr) => (
                  <label key={addr.id} className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3 text-sm ${billingId === addr.id ? 'border-brand-500 bg-brand-50' : 'border-ink/10'}`}>
                    <input type="radio" name="billing" className="mt-1" checked={billingId === addr.id} onChange={() => setBillingId(addr.id)} />
                    <span>
                      <span className="font-medium text-ink">{addr.fullName}</span> — {addr.city}, {addr.state}
                    </span>
                  </label>
                ))}
              </div>
            )}
          </section>

          <section className="card p-6">
            <h2 className="font-display font-semibold text-ink flex items-center gap-2">
              <HiOutlineCreditCard className="h-5 w-5" /> Payment method
            </h2>
            <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-3">
              {paymentMethods.map((pm) => (
                <button
                  key={pm.value}
                  onClick={() => setPaymentMethod(pm.value)}
                  className={`rounded-xl border p-3 text-sm font-medium transition-colors ${paymentMethod === pm.value ? 'border-brand-500 bg-brand-50 text-brand-600' : 'border-ink/10 text-ink-soft'}`}
                >
                  {pm.label}
                </button>
              ))}
            </div>
            <p className="mt-3 text-xs text-ink-faint">This is a mock payment gateway for demo purposes — no real charges are made.</p>
          </section>
        </div>

        <div className="card h-fit space-y-3 p-6">
          <h2 className="font-display font-semibold text-ink">Order summary</h2>
          {cart.items.map((item) => (
            <div key={item.id} className="flex justify-between text-sm text-ink-soft">
              <span className="truncate pr-2">{item.productName} × {item.quantity}</span>
              <span className="font-mono">${item.lineTotal.toFixed(2)}</span>
            </div>
          ))}
          <div className="border-t border-ink/5 pt-3 flex justify-between text-sm text-ink-soft">
            <span>Subtotal</span><span className="font-mono">${cart.subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm text-ink-soft">
            <span>Tax</span><span className="font-mono">${cart.tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm text-ink-soft">
            <span>Shipping</span><span className="font-mono">{cart.shipping === 0 ? 'Free' : `$${cart.shipping.toFixed(2)}`}</span>
          </div>
          <div className="border-t border-ink/5 pt-3 flex justify-between font-semibold text-ink">
            <span>Total</span><span className="font-mono">${cart.grandTotal.toFixed(2)}</span>
          </div>
          <button onClick={placeOrder} disabled={placing} className="btn-primary w-full mt-2">
            {placing ? 'Placing order…' : 'Place order'}
          </button>
        </div>
      </div>
    </div>
  )
}
