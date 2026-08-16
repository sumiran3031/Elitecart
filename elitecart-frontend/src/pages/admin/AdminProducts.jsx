import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { HiOutlinePlus, HiOutlinePencil, HiOutlineTrash, HiOutlineX } from 'react-icons/hi'
import { productApi } from '../../api/productApi.js'
import { categoryApi } from '../../api/categoryApi.js'
import { adminProductApi } from '../../api/adminApi.js'
import Pagination from '../../components/common/Pagination.jsx'

const emptyForm = {
  name: '', sku: '', description: '', price: '', discountPrice: '',
  stockQuantity: '', categoryId: '', featured: false, active: true, imageUrl: '',
}

export default function AdminProducts() {
  const [productPage, setProductPage] = useState(null)
  const [categories, setCategories] = useState([])
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(null) // null = closed, {} = new, {...} = editing
  const { register, handleSubmit, reset, formState: { errors } } = useForm({ defaultValues: emptyForm })

  const loadProducts = () => {
    setLoading(true)
    productApi.search({ page, size: 10, sort: 'createdAt,desc' })
      .then((res) => setProductPage(res.data.data))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadProducts() }, [page])
  useEffect(() => { categoryApi.getAll().then((res) => setCategories(res.data.data)) }, [])

  const openCreate = () => {
    reset(emptyForm)
    setEditing({})
  }

  const openEdit = (product) => {
    reset({
      name: product.name,
      sku: product.sku,
      description: product.description || '',
      price: product.price,
      discountPrice: product.discountPrice || '',
      stockQuantity: product.stockQuantity,
      categoryId: product.categoryId,
      featured: product.featured,
      active: product.active,
      imageUrl: product.images?.[0]?.imageUrl || '',
    })
    setEditing(product)
  }

  const closeForm = () => setEditing(null)

  const onSubmit = async (formData) => {
    const payload = {
      name: formData.name,
      sku: formData.sku,
      description: formData.description,
      price: Number(formData.price),
      discountPrice: formData.discountPrice ? Number(formData.discountPrice) : null,
      stockQuantity: Number(formData.stockQuantity),
      categoryId: Number(formData.categoryId),
      featured: formData.featured,
      active: formData.active,
      images: formData.imageUrl ? [{ imageUrl: formData.imageUrl, primary: true, displayOrder: 0 }] : [],
    }
    try {
      if (editing?.id) {
        await adminProductApi.update(editing.id, payload)
        toast.success('Product updated')
      } else {
        await adminProductApi.create(payload)
        toast.success('Product created')
      }
      closeForm()
      loadProducts()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not save product')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product? This cannot be undone.')) return
    try {
      await adminProductApi.remove(id)
      toast.success('Product deleted')
      loadProducts()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not delete product')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-ink">Products</h1>
        <button onClick={openCreate} className="btn-primary text-sm"><HiOutlinePlus className="h-4 w-4" /> New product</button>
      </div>

      {editing !== null && (
        <div className="card mt-6 p-6">
          <div className="flex items-center justify-between">
            <h2 className="font-display font-semibold text-ink">{editing?.id ? 'Edit product' : 'New product'}</h2>
            <button onClick={closeForm} className="btn-ghost"><HiOutlineX className="h-4 w-4" /></button>
          </div>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-4 grid grid-cols-2 gap-3">
            <input className="input-field col-span-2" placeholder="Product name" {...register('name', { required: true })} />
            <input className="input-field" placeholder="SKU" {...register('sku', { required: true })} />
            <select className="input-field" {...register('categoryId', { required: true })}>
              <option value="">Select category</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <input type="number" step="0.01" className="input-field" placeholder="Price" {...register('price', { required: true })} />
            <input type="number" step="0.01" className="input-field" placeholder="Discount price (optional)" {...register('discountPrice')} />
            <input type="number" className="input-field" placeholder="Stock quantity" {...register('stockQuantity', { required: true })} />
            <input className="input-field" placeholder="Image URL" {...register('imageUrl')} />
            <textarea className="input-field col-span-2" rows={3} placeholder="Description" {...register('description')} />
            <label className="flex items-center gap-2 text-sm text-ink-soft"><input type="checkbox" {...register('featured')} /> Featured</label>
            <label className="flex items-center gap-2 text-sm text-ink-soft"><input type="checkbox" {...register('active')} /> Active</label>
            <div className="col-span-2 flex gap-2">
              <button type="submit" className="btn-primary">Save product</button>
              <button type="button" onClick={closeForm} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="card mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-ink/5 text-left text-ink-faint">
              <th className="p-4 font-medium">Product</th>
              <th className="p-4 font-medium">SKU</th>
              <th className="p-4 font-medium">Price</th>
              <th className="p-4 font-medium">Stock</th>
              <th className="p-4 font-medium">Status</th>
              <th className="p-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {!loading && productPage?.content?.map((p) => (
              <tr key={p.id} className="border-b border-ink/5 last:border-0">
                <td className="p-4 font-medium text-ink">{p.name}</td>
                <td className="p-4 font-mono text-xs text-ink-faint">{p.sku}</td>
                <td className="p-4 font-mono">₹{p.effectivePrice?.toFixed(2)}</td>
                <td className="p-4">{p.stockQuantity}</td>
                <td className="p-4">
                  <span className={`badge ${p.active ? 'bg-accent-emerald/10 text-accent-emerald' : 'bg-ink/5 text-ink-faint'}`}>
                    {p.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="p-4 text-right">
                  <button onClick={() => openEdit(p)} className="btn-ghost p-2"><HiOutlinePencil className="h-4 w-4" /></button>
                  <button onClick={() => handleDelete(p.id)} className="btn-ghost p-2 text-accent-rose"><HiOutlineTrash className="h-4 w-4" /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p className="p-6 text-center text-ink-faint">Loading products…</p>}
      </div>

      {productPage && <Pagination page={productPage.number} totalPages={productPage.totalPages} onPageChange={setPage} />}
    </div>
  )
}
