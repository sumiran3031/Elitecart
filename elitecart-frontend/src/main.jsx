import React from 'react'
import ReactDOM from 'react-dom/client'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App.jsx'
import { store } from './app/store.js'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
        <Toaster
          position="top-right"
          toastOptions={{
            className: 'font-body text-sm',
            style: {
              borderRadius: '12px',
              background: '#15151A',
              color: '#FAFAF9',
            },
          }}
        />
      </BrowserRouter>
    </Provider>
  </React.StrictMode>
)
