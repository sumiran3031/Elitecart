import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { HiOutlinePlus, HiOutlinePencil, HiOutlineTrash, HiOutlineX } from 'react-icons/hi'
import { categoryApi } from '../../api/categoryApi.js'
import { adminCategoryApi } from '../../api/adminApi.js'

const emptyForm = { name: '', description: '', imageUrl: '', parentId: '', active: true }

export default function AdminCategories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(null)
  const { register, handleSubmit, reset } = useForm({ defaultValues: emptyForm })

  const load = () => {
    setLoading(true)
    categoryApi.getAll().then((res) => setCategories(res.data.data)).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openCreate = () => { reset(emptyForm); setEditing({}) }
  const openEdit = (cat) => {
    reset({
      name: cat.name, description: cat.description || '', imageUrl: cat.imageUrl || '',
      parentId: cat.parentId || '', active: cat.active,
    })
    setEditing(cat)
  }
  const closeForm = () => setEditing(null)

  const onSubmit = async (formData) => {
    const payload = {
      name: formData.name,
      description: formData.description,
      imageUrl: formData.imageUrl,
      parentId: formData.parentId ? Number(formData.parentId) : null,
      active: formData.active,
    }
    try {
      if (editing?.id) {
        await adminCategoryApi.update(editing.id, payload)
        toast.success('Category updated')
      } else {
        await adminCategoryApi.create(payload)
        toast.success('Category created')
      }
      closeForm()
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not save category')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this category?')) return
    try {
      await adminCategoryApi.remove(id)
      toast.success('Category deleted')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not delete category (it may still have products)')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-ink">Categories</h1>
        <button onClick={openCreate} className="btn-primary text-sm"><HiOutlinePlus className="h-4 w-4" /> New category</button>
      </div>

      {editing !== null && (
        <div className="card mt-6 p-6">
          <div className="flex items-center justify-between">
            <h2 className="font-display font-semibold text-ink">{editing?.id ? 'Edit category' : 'New category'}</h2>
            <button onClick={closeForm} className="btn-ghost"><HiOutlineX className="h-4 w-4" /></button>
          </div>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-4 grid grid-cols-2 gap-3">
            <input className="input-field col-span-2" placeholder="Category name" {...register('name', { required: true })} />
            <select className="input-field col-span-2" {...register('parentId')}>
              <option value="">No parent (top-level)</option>
              {categories.filter((c) => c.id !== editing?.id).map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <input className="input-field col-span-2" placeholder="Image URL" {...register('imageUrl')} />
            <textarea className="input-field col-span-2" rows={2} placeholder="Description" {...register('description')} />
            <label className="flex items-center gap-2 text-sm text-ink-soft"><input type="checkbox" {...register('active')} /> Active</label>
            <div className="col-span-2 flex gap-2">
              <button type="submit" className="btn-primary">Save category</button>
              <button type="button" onClick={closeForm} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="card mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-ink/5 text-left text-ink-faint">
              <th className="p-4 font-medium">Name</th>
              <th className="p-4 font-medium">Parent</th>
              <th className="p-4 font-medium">Products</th>
              <th className="p-4 font-medium">Status</th>
              <th className="p-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {!loading && categories.map((c) => (
              <tr key={c.id} className="border-b border-ink/5 last:border-0">
                <td className="p-4 font-medium text-ink">{c.name}</td>
                <td className="p-4 text-ink-faint">{c.parentName || '—'}</td>
                <td className="p-4">{c.productCount}</td>
                <td className="p-4">
                  <span className={`badge ${c.active ? 'bg-accent-emerald/10 text-accent-emerald' : 'bg-ink/5 text-ink-faint'}`}>
                    {c.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="p-4 text-right">
                  <button onClick={() => openEdit(c)} className="btn-ghost p-2"><HiOutlinePencil className="h-4 w-4" /></button>
                  <button onClick={() => handleDelete(c.id)} className="btn-ghost p-2 text-accent-rose"><HiOutlineTrash className="h-4 w-4" /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p className="p-6 text-center text-ink-faint">Loading categories…</p>}
      </div>
    </div>
  )
}
