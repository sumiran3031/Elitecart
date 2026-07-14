import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import toast from 'react-hot-toast'
import { authApi } from '../../api/authApi.js'

const storedUser = localStorage.getItem('user')

const initialState = {
  user: storedUser ? JSON.parse(storedUser) : null,
  accessToken: localStorage.getItem('accessToken') || null,
  status: 'idle', // idle | loading | succeeded | failed
  error: null,
}

function persistSession(authResponse) {
  localStorage.setItem('accessToken', authResponse.accessToken)
  localStorage.setItem('refreshToken', authResponse.refreshToken)
  localStorage.setItem('user', JSON.stringify(authResponse.user))
}

function clearSession() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

export const registerUser = createAsyncThunk('auth/register', async (payload, { rejectWithValue }) => {
  try {
    const { data } = await authApi.register(payload)
    return data.message
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Registration failed')
  }
})

export const loginUser = createAsyncThunk('auth/login', async (payload, { rejectWithValue }) => {
  try {
    const { data } = await authApi.login(payload)
    persistSession(data.data)
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Invalid email or password')
  }
})

export const logoutUser = createAsyncThunk('auth/logout', async () => {
  try {
    await authApi.logout()
  } catch {
    // best-effort; clear local session regardless
  }
  clearSession()
})

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearAuthError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = 'succeeded'
        state.user = action.payload.user
        state.accessToken = action.payload.accessToken
        toast.success(`Welcome back, ${action.payload.user.firstName}!`)
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = 'failed'
        state.error = action.payload
        toast.error(action.payload)
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        toast.success(action.payload)
      })
      .addCase(registerUser.rejected, (state, action) => {
        toast.error(action.payload)
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null
        state.accessToken = null
        toast.success('Logged out successfully')
      })
  },
})

export const { clearAuthError } = authSlice.actions
export default authSlice.reducer
