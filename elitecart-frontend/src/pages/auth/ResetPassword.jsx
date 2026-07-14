import { useForm } from 'react-hook-form'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { HiOutlineLockClosed } from 'react-icons/hi'
import AuthLayout from './AuthLayout.jsx'
import { authApi } from '../../api/authApi.js'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const { register, handleSubmit, watch, formState: { errors } } = useForm()
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const newPassword = watch('newPassword')

  const onSubmit = async (formData) => {
    setLoading(true)
    try {
      const { data } = await authApi.resetPassword({ token, newPassword: formData.newPassword })
      toast.success(data.message)
      navigate('/login')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Reset link may have expired')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <AuthLayout title="Invalid link">
        <p className="text-sm text-ink-faint">
          This reset link is missing its token. Please request a new one from the{' '}
          <Link to="/forgot-password" className="text-brand-500 hover:underline">forgot password</Link> page.
        </p>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title="Set a new password">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label" htmlFor="newPassword">New password</label>
          <div className="relative">
            <HiOutlineLockClosed className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input id="newPassword" type="password" className="input-field pl-10" placeholder="••••••••"
              {...register('newPassword', {
                required: 'Password is required',
                minLength: { value: 8, message: 'At least 8 characters' },
                pattern: {
                  value: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$/,
                  message: 'Needs an uppercase, lowercase letter and a digit',
                },
              })} />
          </div>
          {errors.newPassword && <p className="mt-1 text-xs text-accent-rose">{errors.newPassword.message}</p>}
        </div>
        <div>
          <label className="label" htmlFor="confirmPassword">Confirm password</label>
          <input id="confirmPassword" type="password" className="input-field" placeholder="••••••••"
            {...register('confirmPassword', {
              validate: (value) => value === newPassword || 'Passwords do not match',
            })} />
          {errors.confirmPassword && <p className="mt-1 text-xs text-accent-rose">{errors.confirmPassword.message}</p>}
        </div>
        <button type="submit" className="btn-primary w-full" disabled={loading}>
          {loading ? 'Resetting…' : 'Reset password'}
        </button>
      </form>
    </AuthLayout>
  )
}
