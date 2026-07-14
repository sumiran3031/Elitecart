import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { HiOutlineMail, HiOutlineLockClosed, HiOutlineEye, HiOutlineEyeOff } from 'react-icons/hi'
import AuthLayout from './AuthLayout.jsx'
import { loginUser } from '../../features/auth/authSlice.js'

export default function Login() {
  const [showPassword, setShowPassword] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const { status } = useSelector((state) => state.auth)

  const onSubmit = async (formData) => {
    const result = await dispatch(loginUser(formData))
    if (loginUser.fulfilled.match(result)) {
      navigate(location.state?.from?.pathname || '/', { replace: true })
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to continue to your account">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label" htmlFor="email">Email</label>
          <div className="relative">
            <HiOutlineMail className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input
              id="email"
              type="email"
              className="input-field pl-10"
              placeholder="you@example.com"
              {...register('email', { required: 'Email is required' })}
            />
          </div>
          {errors.email && <p className="mt-1 text-xs text-accent-rose">{errors.email.message}</p>}
        </div>

        <div>
          <div className="flex items-center justify-between">
            <label className="label" htmlFor="password">Password</label>
            <Link to="/forgot-password" className="text-xs font-medium text-brand-500 hover:underline">
              Forgot password?
            </Link>
          </div>
          <div className="relative">
            <HiOutlineLockClosed className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              className="input-field pl-10 pr-10"
              placeholder="••••••••"
              {...register('password', { required: 'Password is required' })}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3.5 top-3.5 text-ink-faint"
            >
              {showPassword ? <HiOutlineEyeOff className="h-4 w-4" /> : <HiOutlineEye className="h-4 w-4" />}
            </button>
          </div>
          {errors.password && <p className="mt-1 text-xs text-accent-rose">{errors.password.message}</p>}
        </div>

        <label className="flex items-center gap-2 text-sm text-ink-soft">
          <input type="checkbox" className="rounded border-ink/20" {...register('rememberMe')} />
          Remember me
        </label>

        <button type="submit" className="btn-primary w-full" disabled={status === 'loading'}>
          {status === 'loading' ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-faint">
        Don't have an account?{' '}
        <Link to="/register" className="font-medium text-brand-500 hover:underline">Create one</Link>
      </p>
    </AuthLayout>
  )
}
