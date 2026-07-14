import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { HiOutlineRefresh } from 'react-icons/hi'
import { adminOrderApi, adminPaymentApi } from '../../api/adminApi.js'
import Pagination from '../../components/common/Pagination.jsx'

const statuses = ['PENDING', 'CONFIRMED', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

const statusColors = {
  PENDING: 'bg-accent-amber/10 text-accent-amber',
  CONFIRMED: 'bg-brand-50 text-brand-600',
  PACKED: 'bg-brand-50 text-brand-600',
  SHIPPED: 'bg-brand-50 text-brand-600',
  DELIVERED: 'bg-accent-emerald/10 text-accent-emerald',
  CANCELLED: 'bg-accent-rose/10 text-accent-rose',
}

export default function AdminOrders() {
  const [orderPage, setOrderPage] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    adminOrderApi.getAll({ page, size: 15 }).then((res) => setOrderPage(res.data.data)).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [page])

  const handleStatusChange = async (id, status) => {
    try {
      await adminOrderApi.updateStatus(id, status)
      toast.success(`Order updated to ${status}`)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not update status')
    }
  }

  const handleRefund = async (orderId) => {
    if (!window.confirm('Issue a mock refund for this order?')) return
    try {
      await adminPaymentApi.refund(orderId)
      toast.success('Refund processed')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not process refund')
    }
  }

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-ink">Orders</h1>

      <div className="card mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-ink/5 text-left text-ink-faint">
              <th className="p-4 font-medium">Order</th>
              <th className="p-4 font-medium">Date</th>
              <th className="p-4 font-medium">Total</th>
              <th className="p-4 font-medium">Payment</th>
              <th className="p-4 font-medium">Status</th>
              <th className="p-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {!loading && orderPage?.content?.map((order) => (
              <tr key={order.id} className="border-b border-ink/5 last:border-0">
                <td className="p-4 font-medium text-ink">{order.orderNumber}</td>
                <td className="p-4 text-ink-faint">{new Date(order.createdAt).toLocaleDateString()}</td>
                <td className="p-4 font-mono">${order.grandTotal.toFixed(2)}</td>
                <td className="p-4">
                  <span className="badge bg-ink/5 text-ink-soft">{order.payment?.status || '—'}</span>
                </td>
                <td className="p-4">
                  <select
                    value={order.status}
                    onChange={(e) => handleStatusChange(order.id, e.target.value)}
                    className={`rounded-full border-0 px-2.5 py-1 text-xs font-medium ${statusColors[order.status]}`}
                  >
                    {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                </td>
                <td className="p-4 text-right">
                  {order.payment?.status === 'SUCCESS' && (
                    <button onClick={() => handleRefund(order.id)} className="btn-ghost p-2" title="Refund">
                      <HiOutlineRefresh className="h-4 w-4" />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p className="p-6 text-center text-ink-faint">Loading orders…</p>}
      </div>

      {orderPage && <Pagination page={orderPage.number} totalPages={orderPage.totalPages} onPageChange={setPage} />}
    </div>
  )
}
