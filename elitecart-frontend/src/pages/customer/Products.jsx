import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import { HiOutlineFilter, HiOutlineX, HiOutlineSearch } from 'react-icons/hi'
import { productApi } from '../../api/productApi.js'
import { categoryApi } from '../../api/categoryApi.js'
import ProductCard from '../../components/product/ProductCard.jsx'
import { ProductGridSkeleton } from '../../components/common/LoadingSkeleton.jsx'
import EmptyState from '../../components/common/EmptyState.jsx'
import Pagination from '../../components/common/Pagination.jsx'

const sortOptions = [
  { value: 'createdAt,desc', label: 'Newest' },
  { value: 'price,asc', label: 'Price: Low to High' },
  { value: 'price,desc', label: 'Price: High to Low' },
  { value: 'rating,desc', label: 'Top Rated' },
]

export default function Products() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [categories, setCategories] = useState([])
  const [productPage, setProductPage] = useState(null)
  const [loading, setLoading] = useState(true)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') || '')

  const page = parseInt(searchParams.get('page') || '0', 10)
  const sort = searchParams.get('sort') || 'createdAt,desc'
  const categoryId = searchParams.get('categoryId') || ''
  const featured = searchParams.get('featured') || ''
  const minPrice = searchParams.get('minPrice') || ''
  const maxPrice = searchParams.get('maxPrice') || ''

  useEffect(() => {
    categoryApi.getAll().then((res) => setCategories(res.data.data))
  }, [])

  const fetchProducts = useCallback(() => {
    setLoading(true)
    const params = {
      page,
      size: 12,
      sort,
      keyword: searchParams.get('keyword') || undefined,
      categoryId: categoryId || undefined,
      featured: featured || undefined,
      minPrice: minPrice || undefined,
      maxPrice: maxPrice || undefined,
    }
    productApi
      .search(params)
      .then((res) => setProductPage(res.data.data))
      .finally(() => setLoading(false))
  }, [page, sort, categoryId, featured, minPrice, maxPrice, searchParams])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  const updateParam = (key, value) => {
    const next = new URLSearchParams(searchParams)
    if (value === '' || value === undefined || value === null) {
      next.delete(key)
    } else {
      next.set(key, value)
    }
    if (key !== 'page') {
      next.set('page', '0')
    }
    setSearchParams(next)
  }

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    updateParam('keyword', keywordInput)
  }

  const clearFilters = () => {
    setKeywordInput('')
    setSearchParams({})
  }

  const activeFilterCount = [categoryId, featured, minPrice, maxPrice, searchParams.get('keyword')].filter(Boolean).length

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="flex flex-col gap-6 lg:flex-row">
        {/* Filters sidebar */}
        <aside className={`lg:w-64 lg:shrink-0 ${filtersOpen ? 'block' : 'hidden lg:block'}`}>
          <div className="card sticky top-24 space-y-6 p-5">
            <div className="flex items-center justify-between">
              <h3 className="font-display font-semibold text-ink">Filters</h3>
              {activeFilterCount > 0 && (
                <button onClick={clearFilters} className="text-xs text-brand-500 hover:underline">Clear all</button>
              )}
            </div>

            <div>
              <p className="label">Category</p>
              <div className="space-y-1.5">
                <button
                  onClick={() => updateParam('categoryId', '')}
                  className={`block w-full rounded-lg px-2 py-1.5 text-left text-sm ${!categoryId ? 'bg-brand-50 text-brand-600 font-medium' : 'text-ink-soft hover:bg-ink/5'}`}
                >
                  All categories
                </button>
                {categories.map((cat) => (
                  <button
                    key={cat.id}
                    onClick={() => updateParam('categoryId', cat.id)}
                    className={`block w-full rounded-lg px-2 py-1.5 text-left text-sm ${categoryId == cat.id ? 'bg-brand-50 text-brand-600 font-medium' : 'text-ink-soft hover:bg-ink/5'}`}
                  >
                    {cat.name} <span className="text-xs text-ink-faint">({cat.productCount})</span>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <p className="label">Price range</p>
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  placeholder="Min"
                  className="input-field py-2 text-sm"
                  defaultValue={minPrice}
                  onBlur={(e) => updateParam('minPrice', e.target.value)}
                />
                <span className="text-ink-faint">–</span>
                <input
                  type="number"
                  placeholder="Max"
                  className="input-field py-2 text-sm"
                  defaultValue={maxPrice}
                  onBlur={(e) => updateParam('maxPrice', e.target.value)}
                />
              </div>
            </div>

            <label className="flex items-center gap-2 text-sm text-ink-soft">
              <input
                type="checkbox"
                className="rounded border-ink/20"
                checked={featured === 'true'}
                onChange={(e) => updateParam('featured', e.target.checked ? 'true' : '')}
              />
              Featured only
            </label>
          </div>
        </aside>

        {/* Main content */}
        <div className="flex-1">
          <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <form onSubmit={handleSearchSubmit} className="relative flex-1 sm:max-w-sm">
              <HiOutlineSearch className="absolute left-3.5 top-3.5 h-4 w-4 text-ink-faint" />
              <input
                className="input-field pl-10"
                placeholder="Search products…"
                value={keywordInput}
                onChange={(e) => setKeywordInput(e.target.value)}
              />
            </form>

            <div className="flex items-center gap-2">
              <button onClick={() => setFiltersOpen(!filtersOpen)} className="btn-secondary lg:hidden text-sm">
                {filtersOpen ? <HiOutlineX className="h-4 w-4" /> : <HiOutlineFilter className="h-4 w-4" />}
                Filters {activeFilterCount > 0 && `(${activeFilterCount})`}
              </button>
              <select
                className="input-field w-auto py-2.5 text-sm"
                value={sort}
                onChange={(e) => updateParam('sort', e.target.value)}
              >
                {sortOptions.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>
          </div>

          {loading ? (
            <ProductGridSkeleton count={12} />
          ) : productPage?.content?.length ? (
            <>
              <p className="mb-4 text-sm text-ink-faint">{productPage.totalElements} products found</p>
              <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 xl:grid-cols-4">
                {productPage.content.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
              <Pagination
                page={productPage.number}
                totalPages={productPage.totalPages}
                onPageChange={(p) => updateParam('page', p)}
              />
            </>
          ) : (
            <EmptyState
              icon={HiOutlineSearch}
              title="No products found"
              description="Try adjusting your filters or search term."
              action={<button onClick={clearFilters} className="btn-secondary">Clear filters</button>}
            />
          )}
        </div>
      </div>
    </div>
  )
}
