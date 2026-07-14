import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import toast from 'react-hot-toast'
import { wishlistApi } from '../../api/wishlistApi.js'

const initialState = {
  wishlist: null, // { id, items }
  status: 'idle',
  error: null,
}

export const fetchWishlist = createAsyncThunk('wishlist/fetch', async (_, { rejectWithValue }) => {
  try {
    const { data } = await wishlistApi.getWishlist()
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to load wishlist')
  }
})

export const addToWishlist = createAsyncThunk('wishlist/addItem', async (productId, { rejectWithValue }) => {
  try {
    const { data } = await wishlistApi.addItem({ productId })
    toast.success('Added to wishlist')
    return data.data
  } catch (err) {
    const message = err.response?.data?.message || 'Could not add to wishlist'
    toast.error(message)
    return rejectWithValue(message)
  }
})

export const removeFromWishlist = createAsyncThunk('wishlist/removeItem', async (itemId, { rejectWithValue }) => {
  try {
    const { data } = await wishlistApi.removeItem(itemId)
    toast.success('Removed from wishlist')
    return data.data
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Could not remove item')
  }
})

export const moveWishlistItemToCart = createAsyncThunk('wishlist/moveToCart', async (itemId, { rejectWithValue }) => {
  try {
    await wishlistApi.moveToCart(itemId)
    toast.success('Moved to cart')
    const { data } = await wishlistApi.getWishlist()
    return data.data
  } catch (err) {
    const message = err.response?.data?.message || 'Could not move item to cart'
    toast.error(message)
    return rejectWithValue(message)
  }
})

const wishlistSlice = createSlice({
  name: 'wishlist',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addMatcher(
        (action) => action.type.startsWith('wishlist/') && action.type.endsWith('/fulfilled'),
        (state, action) => {
          state.status = 'succeeded'
          state.wishlist = action.payload
        }
      )
      .addMatcher(
        (action) => action.type.startsWith('wishlist/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.status = 'failed'
          state.error = action.payload
        }
      )
  },
})

export default wishlistSlice.reducer
