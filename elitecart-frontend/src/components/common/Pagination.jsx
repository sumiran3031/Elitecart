import { HiChevronLeft, HiChevronRight } from 'react-icons/hi'

export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const pages = Array.from({ length: totalPages }, (_, i) => i).filter(
    (p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1
  )

  return (
    <div className="flex items-center justify-center gap-1 mt-10">
      <button
        className="btn-ghost disabled:opacity-30"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        aria-label="Previous page"
      >
        <HiChevronLeft className="h-5 w-5" />
      </button>

      {pages.map((p, idx) => (
        <span key={p} className="flex items-center">
          {idx > 0 && p - pages[idx - 1] > 1 && <span className="px-1 text-ink-faint">…</span>}
          <button
            onClick={() => onPageChange(p)}
            className={`h-9 w-9 rounded-full text-sm font-medium transition-colors ${
              p === page ? 'bg-brand-gradient text-white' : 'text-ink-soft hover:bg-ink/5'
            }`}
          >
            {p + 1}
          </button>
        </span>
      ))}

      <button
        className="btn-ghost disabled:opacity-30"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        aria-label="Next page"
      >
        <HiChevronRight className="h-5 w-5" />
      </button>
    </div>
  )
}
