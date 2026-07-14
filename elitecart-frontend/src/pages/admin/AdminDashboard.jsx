import { useEffect, useState } from 'react'
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  PieChart, Pie, Cell, BarChart, Bar,
} from 'recharts'
import { HiOutlineCurrencyDollar, HiOutlineShoppingBag, HiOutlineCube, HiOutlineUsers, HiOutlineDownload } from 'react-icons/hi'
import { adminAnalyticsApi } from '../../api/adminApi.js'

const COLORS = ['#6366F1', '#8B5CF6', '#F59E0B', '#10B981', '#EF4444', '#7C7FF2']

function StatCard({ icon: Icon, label, value }) {
  return (
    <div className="card p-5">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
          <Icon className="h-5 w-5" />
        </div>
        <div>
          <p className="text-xs text-ink-faint">{label}</p>
          <p className="font-display text-xl font-bold text-ink">{value}</p>
        </div>
      </div>
    </div>
  )
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)

  useEffect(() => {
    adminAnalyticsApi.getDashboard().then((res) => setStats(res.data.data)).finally(() => setLoading(false))
  }, [])

  const handleExport = async () => {
    setExporting(true)
    try {
      const res = await adminAnalyticsApi.exportOrdersCsv()
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', 'elitecart-orders.csv')
      document.body.appendChild(link)
      link.click()
      link.remove()
    } finally {
      setExporting(false)
    }
  }

  if (loading || !stats) {
    return <div className="text-center text-ink-faint py-24">Loading dashboard…</div>
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-ink">Dashboard</h1>
        <button onClick={handleExport} disabled={exporting} className="btn-secondary text-sm">
          <HiOutlineDownload className="h-4 w-4" /> {exporting ? 'Exporting…' : 'Export orders CSV'}
        </button>
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard icon={HiOutlineCurrencyDollar} label="Total revenue" value={`$${stats.totalRevenue.toFixed(2)}`} />
        <StatCard icon={HiOutlineShoppingBag} label="Total orders" value={stats.totalOrders} />
        <StatCard icon={HiOutlineCube} label="Total products" value={stats.totalProducts} />
        <StatCard icon={HiOutlineUsers} label="Total customers" value={stats.totalCustomers} />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="card p-6 lg:col-span-2">
          <h2 className="font-display font-semibold text-ink">Revenue — last 12 months</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={stats.monthlySales}>
                <CartesianGrid strokeDasharray="3 3" stroke="#15151A0D" />
                <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#8A8A97' }} />
                <YAxis tick={{ fontSize: 12, fill: '#8A8A97' }} />
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                <Line type="monotone" dataKey="revenue" stroke="#6366F1" strokeWidth={2.5} dot={{ r: 3 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card p-6">
          <h2 className="font-display font-semibold text-ink">Revenue by category</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={stats.categorySales} dataKey="revenue" nameKey="categoryName" innerRadius={50} outerRadius={80} paddingAngle={2}>
                  {stats.categorySales.map((entry, idx) => (
                    <Cell key={entry.categoryName} fill={COLORS[idx % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card p-6">
          <h2 className="font-display font-semibold text-ink">Latest orders</h2>
          <div className="mt-4 space-y-3">
            {stats.latestOrders.slice(0, 6).map((order) => (
              <div key={order.id} className="flex items-center justify-between text-sm">
                <span className="text-ink-soft">{order.orderNumber}</span>
                <span className="badge bg-ink/5 text-ink-soft">{order.status}</span>
                <span className="font-mono font-medium text-ink">${order.grandTotal.toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="card p-6">
          <h2 className="font-display font-semibold text-ink">Top products by units sold</h2>
          <div className="mt-4 h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={stats.topProducts} layout="vertical" margin={{ left: 20 }}>
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" width={120} tick={{ fontSize: 11, fill: '#4B4B57' }} />
                <Tooltip />
                <Bar dataKey="unitsSold" fill="#8B5CF6" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  )
}
