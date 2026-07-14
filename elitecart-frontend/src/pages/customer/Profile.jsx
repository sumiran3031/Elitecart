import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useSelector } from 'react-redux'
import toast from 'react-hot-toast'
import { HiOutlineTrash, HiOutlinePlus } from 'react-icons/hi'
import { addressApi } from '../../api/addressApi.js'
import { authApi } from '../../api/authApi.js'

export default function Profile() {
  const { user } = useSelector((state) => state.auth)
  const [addresses, setAddresses] = useState([])
  const [showAddForm, setShowAddForm] = useState(false)
  const { register, handleSubmit, reset } = useForm()
  const passwordForm = useForm()

  const loadAddresses = () => {
    addressApi.getAll().then((res) => setAddresses(res.data.data))
  }

  useEffect(() => {
    loadAddresses()
  }, [])

  const submitAddress = async (formData) => {
    try {
      await addressApi.create(formData)
      toast.success('Address added')
      reset()
      setShowAddForm(false)
      loadAddresses()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not add address')
    }
  }

  const deleteAddress = async (id) => {
    try {
      await addressApi.remove(id)
      toast.success('Address removed')
      loadAddresses()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not remove address')
    }
  }

  const submitPasswordChange = async (formData) => {
    try {
      const { data } = await authApi.changePassword(formData)
      toast.success(data.message)
      passwordForm.reset()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not change password')
    }
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold text-ink">My profile</h1>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <section className="card p-6">
          <h2 className="font-display font-semibold text-ink">Account details</h2>
          <dl className="mt-4 space-y-2 text-sm">
            <div className="flex justify-between"><dt className="text-ink-faint">Name</dt><dd className="text-ink">{user?.firstName} {user?.lastName}</dd></div>
            <div className="flex justify-between"><dt className="text-ink-faint">Email</dt><dd className="text-ink">{user?.email}</dd></div>
            <div className="flex justify-between"><dt className="text-ink-faint">Phone</dt><dd className="text-ink">{user?.phoneNumber || '—'}</dd></div>
          </dl>
        </section>

        <section className="card p-6">
          <h2 className="font-display font-semibold text-ink">Change password</h2>
          <form onSubmit={passwordForm.handleSubmit(submitPasswordChange)} className="mt-4 space-y-3">
            <input type="password" className="input-field" placeholder="Current password"
              {...passwordForm.register('currentPassword', { required: true })} />
            <input type="password" className="input-field" placeholder="New password"
              {...passwordForm.register('newPassword', { required: true, minLength: 8 })} />
            <button type="submit" className="btn-primary w-full">Update password</button>
          </form>
        </section>

        <section className="card p-6 lg:col-span-2">
          <div className="flex items-center justify-between">
            <h2 className="font-display font-semibold text-ink">Saved addresses</h2>
            <button onClick={() => setShowAddForm(!showAddForm)} className="btn-ghost text-xs">
              <HiOutlinePlus className="h-4 w-4" /> Add address
            </button>
          </div>

          <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
            {addresses.map((addr) => (
              <div key={addr.id} className="rounded-xl border border-ink/10 p-3 text-sm">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-ink">{addr.fullName} {addr.isDefault && <span className="badge bg-brand-50 text-brand-600 ml-1">Default</span>}</p>
                    <p className="mt-1 text-ink-soft">{addr.addressLine1}, {addr.city}, {addr.state} {addr.postalCode}</p>
                  </div>
                  <button onClick={() => deleteAddress(addr.id)} className="text-ink-faint hover:text-accent-rose">
                    <HiOutlineTrash className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
            {addresses.length === 0 && <p className="text-sm text-ink-faint">No addresses saved yet.</p>}
          </div>

          {showAddForm && (
            <form onSubmit={handleSubmit(submitAddress)} className="mt-4 grid grid-cols-2 gap-3 border-t border-ink/5 pt-4">
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
      </div>
    </div>
  )
}
