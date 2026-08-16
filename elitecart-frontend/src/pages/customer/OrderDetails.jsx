import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { HiOutlineArrowLeft, HiCheckCircle } from 'react-icons/hi'
import { orderApi } from '../../api/orderApi.js'

const statusColors = {
  PENDING: 'bg-accent-amber/10 text-accent-amber',
  CONFIRMED: 'bg-brand-50 text-brand-600',
  PACKED: 'bg-brand-50 text-brand-600',
  SHIPPED: 'bg-brand-50 text-brand-600',
  DELIVERED: 'bg-accent-emerald/10 text-accent-emerald',
  CANCELLED: 'bg-accent-rose/10 text-accent-rose',
}

const timelineSteps = ['PENDING', 'CONFIRMED', 'PACKED', 'SHIPPED', 'DELIVERED']

export default function OrderDetails() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [cancelling, setCancelling] = useState(false)

  const load = () => {
    setLoading(true)
    orderApi.getById(id).then((res) => setOrder(res.data.data)).finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [id])

  const handleCancel = async () => {
    const reason = window.prompt('Reason for cancellation:')
    if (!reason) return
    setCancelling(true)
    try {
      await orderApi.cancel(id, reason)
      toast.success('Order cancelled')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not cancel order')
    } finally {
      setCancelling(false)
    }
  }

  if (loading) return <div className="mx-auto max-w-4xl px-4 py-24 text-center text-ink-faint">Loading order…</div>
  if (!order) return <div className="mx-auto max-w-4xl px-4 py-24 text-center text-ink-faint">Order not found.</div>

  const currentStepIndex = timelineSteps.indexOf(order.status)
  const isCancelled = order.status === 'CANCELLED'
  const canCancel = ['PENDING', 'CONFIRMED'].includes(order.status)

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <Link to="/orders" className="flex items-center gap-1 text-sm text-ink-faint hover:text-ink">
        <HiOutlineArrowLeft className="h-4 w-4" /> Back to orders
      </Link>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink">{order.orderNumber}</h1>
          <p className="text-sm text-ink-faint">Placed on {new Date(order.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}</p>
        </div>
        <span className={`badge ${statusColors[order.status] || 'bg-ink/5 text-ink-soft'}`}>{order.status}</span>
      </div>

      {!isCancelled && (
        <div className="card mt-6 p-6">
          <div className="flex items-center justify-between">
            {timelineSteps.map((step, idx) => (
              <div key={step} className="flex flex-1 flex-col items-center text-center">
                <div className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold ${idx <= currentStepIndex ? 'bg-brand-gradient text-white' : 'bg-ink/10 text-ink-faint'}`}>
                  {idx <= currentStepIndex ? <HiCheckCircle className="h-5 w-5" /> : idx + 1}
                </div>
                <p className="mt-2 text-xs text-ink-faint">{step}</p>
                {idx < timelineSteps.length - 1 && (
                  <div className={`absolute mt-4 h-0.5 w-full ${idx < currentStepIndex ? 'bg-brand-500' : 'bg-ink/10'}`} style={{ display: 'none' }} />
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {isCancelled && order.cancelledReason && (
        <div className="mt-6 rounded-xl bg-accent-rose/10 p-4 text-sm text-accent-rose">
          Cancelled: {order.cancelledReason}
        </div>
      )}

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 card p-6">
          <h2 className="font-display font-semibold text-ink">Items</h2>
          <div className="mt-4 space-y-3">
            {order.items.map((item) => (
              <div key={item.id} className="flex justify-between text-sm">
                <span className="text-ink-soft">{item.productName} × {item.quantity}</span>
                <span className="font-mono text-ink">₹{item.lineTotal.toFixed(2)}</span>
              </div>
            ))}
          </div>
          <div className="mt-4 space-y-2 border-t border-ink/5 pt-4 text-sm">
            <div className="flex justify-between text-ink-soft"><span>Subtotal</span><span className="font-mono">₹{order.subtotal.toFixed(2)}</span></div>
            <div className="flex justify-between text-ink-soft"><span>Tax</span><span className="font-mono">₹{order.tax.toFixed(2)}</span></div>
            <div className="flex justify-between text-ink-soft"><span>Shipping</span><span className="font-mono">₹{order.shippingFee.toFixed(2)}</span></div>
            <div className="flex justify-between font-semibold text-ink"><span>Total</span><span className="font-mono">₹{order.grandTotal.toFixed(2)}</span></div>
          </div>

          {canCancel && (
            <button onClick={handleCancel} disabled={cancelling} className="btn-secondary mt-6 text-accent-rose">
              {cancelling ? 'Cancelling…' : 'Cancel order'}
            </button>
          )}
        </div>

        <div className="space-y-6">
          <div className="card p-6">
            <h3 className="font-display text-sm font-semibold text-ink">Shipping address</h3>
            <p className="mt-2 text-sm text-ink-soft">
              {order.shippingAddress?.fullName}<br />
              {order.shippingAddress?.addressLine1}<br />
              {order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}
            </p>
          </div>
          {order.payment && (
            <div className="card p-6">
              <h3 className="font-display text-sm font-semibold text-ink">Payment</h3>
              <p className="mt-2 text-sm text-ink-soft">
                Method: {order.payment.method}<br />
                Status: {order.payment.status}<br />
                Transaction: <span className="font-mono text-xs">{order.payment.transactionId}</span>
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
