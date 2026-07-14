import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { HiOutlineShoppingBag } from 'react-icons/hi'
import { orderApi } from '../../api/orderApi.js'
import EmptyState from '../../components/common/EmptyState.jsx'
import Pagination from '../../components/common/Pagination.jsx'

const statusColors = {
  PENDING: 'bg-accent-amber/10 text-accent-amber',
  CONFIRMED: 'bg-brand-50 text-brand-600',
  PACKED: 'bg-brand-50 text-brand-600',
  SHIPPED: 'bg-brand-50 text-brand-600',
  DELIVERED: 'bg-accent-emerald/10 text-accent-emerald',
  CANCELLED: 'bg-accent-rose/10 text-accent-rose',
}

export default function MyOrders() {
  const [orderPage, setOrderPage] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    orderApi
      .getMyOrders({ page, size: 10 })
      .then((res) => setOrderPage(res.data.data))
      .finally(() => setLoading(false))
  }, [page])

  if (loading && !orderPage) {
    return <div className="mx-auto max-w-4xl px-4 py-24 text-center text-ink-faint">Loading orders…</div>
  }

  if (!orderPage || orderPage.content.length === 0) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-10">
        <EmptyState
          icon={HiOutlineShoppingBag}
          title="No orders yet"
          description="Once you place an order, it'll show up here."
          action={<Link to="/products" className="btn-primary">Start shopping</Link>}
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold text-ink">My orders</h1>

      <div className="mt-8 space-y-4">
        {orderPage.content.map((order) => (
          <Link key={order.id} to={`/orders/${order.id}`} className="card card-hover flex items-center justify-between p-5">
            <div>
              <p className="font-medium text-ink">{order.orderNumber}</p>
              <p className="mt-1 text-xs text-ink-faint">
                {new Date(order.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}
                {' · '}{order.items.length} item{order.items.length > 1 ? 's' : ''}
              </p>
            </div>
            <div className="flex items-center gap-4">
              <span className="font-mono text-sm font-semibold text-ink">${order.grandTotal.toFixed(2)}</span>
              <span className={`badge ${statusColors[order.status] || 'bg-ink/5 text-ink-soft'}`}>{order.status}</span>
            </div>
          </Link>
        ))}
      </div>

      <Pagination page={orderPage.number} totalPages={orderPage.totalPages} onPageChange={setPage} />
    </div>
  )
}
