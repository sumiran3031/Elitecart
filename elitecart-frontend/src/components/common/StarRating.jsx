import { HiStar, HiOutlineStar } from 'react-icons/hi'

export default function StarRating({ rating = 0, size = 'h-4 w-4', showValue = false, reviewCount }) {
  const stars = [1, 2, 3, 4, 5]
  return (
    <div className="flex items-center gap-1">
      <div className="flex text-accent-amber">
        {stars.map((star) =>
          star <= Math.round(rating) ? (
            <HiStar key={star} className={size} />
          ) : (
            <HiOutlineStar key={star} className={size} />
          )
        )}
      </div>
      {showValue && <span className="text-xs text-ink-faint">{rating?.toFixed(1)}</span>}
      {typeof reviewCount === 'number' && (
        <span className="text-xs text-ink-faint">({reviewCount})</span>
      )}
    </div>
  )
}
