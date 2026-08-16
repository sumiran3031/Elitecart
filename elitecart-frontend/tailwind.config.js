/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        canvas: '#FFFFFF',
        ink: {
          DEFAULT: '#111111',
          soft: '#4A4A4A',
          faint: '#8A8A8A',
        },
        brand: {
          50: '#F5F5F5',
          100: '#E5E5E5',
          400: '#333333',
          500: '#111111',
          600: '#000000',
          700: '#000000',
        },
        accent: {
          amber: '#B45309',
          emerald: '#15803D',
          rose: '#B91C1C',
        },
      },
      fontFamily: {
        display: ['Inter', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['ui-monospace', 'monospace'],
      },
      backgroundImage: {
        'brand-gradient': 'none',
        'mesh': 'none',
      },
      boxShadow: {
        card: '0 1px 2px rgba(0,0,0,0.04), 0 1px 3px rgba(0,0,0,0.06)',
        'card-hover': '0 2px 4px rgba(0,0,0,0.06), 0 4px 8px rgba(0,0,0,0.08)',
        glass: 'none',
      },
      borderRadius: {
        '2xl': '0.5rem',
        '3xl': '0.75rem',
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(8px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        aurora: 'none',
        'fade-up': 'fade-up 0.4s ease-out both',
      },
    },
  },
  plugins: [],
}