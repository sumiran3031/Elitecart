import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { HiOutlineMail } from 'react-icons/hi'
import AuthLayout from './AuthLayout.jsx'
import { authApi } from '../../api/authApi.js'

export default function ForgotPassword() {
  const { register, handleSubmit, formState: { errors } } = useForm()
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  const onSubmit = async (formData) => {
    setLoading(true)
    try {
      const { data } = await authApi.forgotPassword(formData)
      toast.success(data.message)
      setSent(true)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout title="Reset your password" subtitle="We'll email you a link to reset it">
      {sent ? (
        <div className="rounded-xl bg-accent-emerald/10 p-4 text-sm text-accent-emerald">
          If an account exists with that email, a reset link is on its way. Check your inbox.
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="label" htmlFor="email">Email</label>
            <div className="relative">
              <HiOutlineMail className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
              <input id="email" type="email" className="input-field pl-10" placeholder="you@example.com"
                {...register('email', { required: 'Email is required' })} />
            </div>
            {errors.email && <p className="mt-1 text-xs text-accent-rose">{errors.email.message}</p>}
          </div>
          <button type="submit" className="btn-primary w-full" disabled={loading}>
            {loading ? 'Sending…' : 'Send reset link'}
          </button>
        </form>
      )}

      <p className="mt-6 text-center text-sm text-ink-faint">
        Remembered it? <Link to="/login" className="font-medium text-brand-500 hover:underline">Sign in</Link>
      </p>
    </AuthLayout>
  )
}
