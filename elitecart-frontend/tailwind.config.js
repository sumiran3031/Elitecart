/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        canvas: '#FAFAF9',
        ink: {
          DEFAULT: '#15151A',
          soft: '#4B4B57',
          faint: '#8A8A97',
        },
        brand: {
          50: '#EEF0FF',
          100: '#E0E3FF',
          400: '#7C7FF2',
          500: '#6366F1',
          600: '#5457D6',
          700: '#8B5CF6',
        },
        accent: {
          amber: '#F59E0B',
          emerald: '#10B981',
          rose: '#EF4444',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
        'mesh': 'radial-gradient(at 20% 20%, rgba(99,102,241,0.25) 0px, transparent 50%), radial-gradient(at 80% 0%, rgba(139,92,246,0.2) 0px, transparent 50%), radial-gradient(at 50% 80%, rgba(245,158,11,0.12) 0px, transparent 50%)',
      },
      boxShadow: {
        card: '0 1px 2px rgba(21,21,26,0.04), 0 8px 24px -8px rgba(21,21,26,0.10)',
        'card-hover': '0 4px 8px rgba(21,21,26,0.06), 0 16px 32px -12px rgba(21,21,26,0.16)',
        glass: '0 1px 0 0 rgba(255,255,255,0.4) inset, 0 8px 32px -8px rgba(21,21,26,0.12)',
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
      },
      keyframes: {
        aurora: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(4%, -4%) scale(1.05)' },
        },
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        aurora: 'aurora 12s ease-in-out infinite',
        'fade-up': 'fade-up 0.5s ease-out both',
      },
    },
  },
  plugins: [],
}
