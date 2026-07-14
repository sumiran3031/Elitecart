import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import toast from 'react-hot-toast'
import { cartApi } from '../../api/cartApi.js'

const initialState = {
  cart: null, // { id, items, subtotal, tax, shipping, discount, grandTotal, totalItems }
  status: 'idle',
  error: null,
}

export const fetchCart = createAsyncThunk('cart/fetch', async (_, { rejectWithValue }) => {
  try {
    const { data } = await cartApi.getCart()
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to load cart')
  }
})

export const addToCart = createAsyncThunk('cart/addItem', async (payload, { rejectWithValue }) => {
  try {
    const { data } = await cartApi.addItem(payload)
    toast.success('Added to cart')
    return data.data
  } catch (err) {
    const message = err.response?.data?.message || 'Could not add item to cart'
    toast.error(message)
    return rejectWithValue(message)
  }
})

export const updateCartItem = createAsyncThunk('cart/updateItem', async ({ itemId, quantity }, { rejectWithValue }) => {
  try {
    const { data } = await cartApi.updateItem(itemId, { quantity })
    return data.data
  } catch (err) {
    const message = err.response?.data?.message || 'Could not update quantity'
    toast.error(message)
    return rejectWithValue(message)
  }
})

export const removeCartItem = createAsyncThunk('cart/removeItem', async (itemId, { rejectWithValue }) => {
  try {
    const { data } = await cartApi.removeItem(itemId)
    toast.success('Item removed')
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Could not remove item')
  }
})

export const clearCart = createAsyncThunk('cart/clear', async (_, { rejectWithValue }) => {
  try {
    const { data } = await cartApi.clearCart()
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Could not clear cart')
  }
})

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addMatcher(
        (action) => action.type.startsWith('cart/') && action.type.endsWith('/pending'),
        (state) => {
          state.status = 'loading'
        }
      )
      .addMatcher(
        (action) => action.type.startsWith('cart/') && action.type.endsWith('/fulfilled'),
        (state, action) => {
          state.status = 'succeeded'
          state.cart = action.payload
        }
      )
      .addMatcher(
        (action) => action.type.startsWith('cart/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.status = 'failed'
          state.error = action.payload
        }
      )
  },
})

export default cartSlice.reducer
