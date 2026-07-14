import { useEffect, useState } from 'react'
import { adminCustomerApi } from '../../api/adminApi.js'
import Pagination from '../../components/common/Pagination.jsx'

export default function AdminCustomers() {
  const [customerPage, setCustomerPage] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    adminCustomerApi.getAll({ page, size: 15 }).then((res) => setCustomerPage(res.data.data)).finally(() => setLoading(false))
  }, [page])

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-ink">Customers</h1>

      <div className="card mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-ink/5 text-left text-ink-faint">
              <th className="p-4 font-medium">Name</th>
              <th className="p-4 font-medium">Email</th>
              <th className="p-4 font-medium">Phone</th>
              <th className="p-4 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {!loading && customerPage?.content?.map((customer) => (
              <tr key={customer.id} className="border-b border-ink/5 last:border-0">
                <td className="p-4 font-medium text-ink">{customer.firstName} {customer.lastName}</td>
                <td className="p-4 text-ink-soft">{customer.email}</td>
                <td className="p-4 text-ink-faint">{customer.phoneNumber || '—'}</td>
                <td className="p-4">
                  <span className={`badge ${customer.enabled ? 'bg-accent-emerald/10 text-accent-emerald' : 'bg-ink/5 text-ink-faint'}`}>
                    {customer.enabled ? 'Verified' : 'Unverified'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p className="p-6 text-center text-ink-faint">Loading customers…</p>}
      </div>

      {customerPage && <Pagination page={customerPage.number} totalPages={customerPage.totalPages} onPageChange={setPage} />}
    </div>
  )
}
