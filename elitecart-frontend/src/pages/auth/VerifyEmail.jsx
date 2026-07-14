import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { HiOutlineCheckCircle, HiOutlineXCircle } from 'react-icons/hi'
import AuthLayout from './AuthLayout.jsx'
import { authApi } from '../../api/authApi.js'

export default function VerifyEmail() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [status, setStatus] = useState('loading') // loading | success | error
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setMessage('Verification link is missing its token.')
      return
    }
    authApi
      .verifyEmail(token)
      .then((res) => {
        setStatus('success')
        setMessage(res.data.message)
      })
      .catch((err) => {
        setStatus('error')
        setMessage(err.response?.data?.message || 'This verification link has expired or is invalid.')
      })
  }, [token])

  return (
    <AuthLayout title="Email verification">
      <div className="flex flex-col items-center text-center">
        {status === 'loading' && <p className="text-sm text-ink-faint">Verifying your email…</p>}
        {status === 'success' && (
          <>
            <HiOutlineCheckCircle className="h-12 w-12 text-accent-emerald" />
            <p className="mt-3 text-sm text-ink-soft">{message}</p>
            <Link to="/login" className="btn-primary mt-6">Go to login</Link>
          </>
        )}
        {status === 'error' && (
          <>
            <HiOutlineXCircle className="h-12 w-12 text-accent-rose" />
            <p className="mt-3 text-sm text-ink-soft">{message}</p>
            <Link to="/login" className="btn-secondary mt-6">Back to login</Link>
          </>
        )}
      </div>
    </AuthLayout>
  )
}
