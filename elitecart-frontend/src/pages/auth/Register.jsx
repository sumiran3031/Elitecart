import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { HiOutlineMail, HiOutlineLockClosed, HiOutlineUser, HiOutlinePhone } from 'react-icons/hi'
import AuthLayout from './AuthLayout.jsx'
import { registerUser } from '../../features/auth/authSlice.js'

export default function Register() {
  const { register, handleSubmit, watch, formState: { errors } } = useForm()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { status } = useSelector((state) => state.auth)
  const password = watch('password')

  const onSubmit = async (formData) => {
    const result = await dispatch(registerUser(formData))
    if (registerUser.fulfilled.match(result)) {
      navigate('/login')
    }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Join EliteCart and start shopping">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label" htmlFor="firstName">First name</label>
            <div className="relative">
              <HiOutlineUser className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
              <input id="firstName" className="input-field pl-10" placeholder="Jane"
                {...register('firstName', { required: 'Required' })} />
            </div>
            {errors.firstName && <p className="mt-1 text-xs text-accent-rose">{errors.firstName.message}</p>}
          </div>
          <div>
            <label className="label" htmlFor="lastName">Last name</label>
            <input id="lastName" className="input-field" placeholder="Doe"
              {...register('lastName', { required: 'Required' })} />
            {errors.lastName && <p className="mt-1 text-xs text-accent-rose">{errors.lastName.message}</p>}
          </div>
        </div>

        <div>
          <label className="label" htmlFor="email">Email</label>
          <div className="relative">
            <HiOutlineMail className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input id="email" type="email" className="input-field pl-10" placeholder="you@example.com"
              {...register('email', { required: 'Email is required' })} />
          </div>
          {errors.email && <p className="mt-1 text-xs text-accent-rose">{errors.email.message}</p>}
        </div>

        <div>
          <label className="label" htmlFor="phoneNumber">Phone number (optional)</label>
          <div className="relative">
            <HiOutlinePhone className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input id="phoneNumber" className="input-field pl-10" placeholder="+1 555 123 4567"
              {...register('phoneNumber')} />
          </div>
        </div>

        <div>
          <label className="label" htmlFor="password">Password</label>
          <div className="relative">
            <HiOutlineLockClosed className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
            <input id="password" type="password" className="input-field pl-10" placeholder="••••••••"
              {...register('password', {
                required: 'Password is required',
                minLength: { value: 8, message: 'At least 8 characters' },
                pattern: {
                  value: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$/,
                  message: 'Needs an uppercase, lowercase letter and a digit',
                },
              })} />
          </div>
          {errors.password && <p className="mt-1 text-xs text-accent-rose">{errors.password.message}</p>}
        </div>

        <div>
          <label className="label" htmlFor="confirmPassword">Confirm password</label>
          <input id="confirmPassword" type="password" className="input-field" placeholder="••••••••"
            {...register('confirmPassword', {
              required: 'Please confirm your password',
              validate: (value) => value === password || 'Passwords do not match',
            })} />
          {errors.confirmPassword && <p className="mt-1 text-xs text-accent-rose">{errors.confirmPassword.message}</p>}
        </div>

        <button type="submit" className="btn-primary w-full" disabled={status === 'loading'}>
          {status === 'loading' ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-faint">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-brand-500 hover:underline">Sign in</Link>
      </p>
    </AuthLayout>
  )
}
