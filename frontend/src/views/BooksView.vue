<template>
  <section class="books-page">
    <section class="catalog-hero">
      <div>
        <p class="eyebrow">馆藏检索</p>
        <h2>{{ canManage ? '馆藏流通管理' : '资源检索' }}</h2>
        <p>{{ canManage ? '维护馆藏、库存、上下架与读者借阅办理。' : '搜索馆藏图书，查看库存并发起线上借阅。' }}</p>
      </div>
      <form class="catalog-search" @submit.prevent="load" aria-label="图书筛选">
        <label>
          关键词
          <input v-model="query.keyword" placeholder="书名 / 作者 / ISBN" />
        </label>
        <label>
          分类
          <select v-model="query.categoryId">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
          </select>
        </label>
        <button class="primary-btn" type="submit">查询</button>
      </form>
    </section>

    <section class="catalog-stats" aria-label="馆藏概览">
      <article>
        <span>当前结果</span>
        <strong>{{ books.length }}</strong>
        <small>本馆藏</small>
      </article>
      <article>
        <span>可借库存</span>
        <strong>{{ availableTotal }}</strong>
        <small>册可借</small>
      </article>
      <article>
        <span>分类数量</span>
        <strong>{{ categories.length }}</strong>
        <small>个分类</small>
      </article>
    </section>

    <section class="table-panel catalog-panel">
      <div class="section-heading">
        <div>
          <h2>馆藏图书</h2>
          <p>按书名、作者、ISBN 或分类快速定位馆藏资源。</p>
        </div>
        <button v-if="canManage" class="ghost-btn" type="button" @click="editing = true">新增图书</button>
      </div>
      <div v-if="books.length" class="book-grid">
        <article v-for="book in books" :key="book.id" class="book-card catalog-card">
          <div class="catalog-cover" :class="{ 'has-image': book.coverUrl }" :style="coverStyle(book)">
            <img v-if="book.coverUrl" :src="assetUrl(book.coverUrl)" :alt="`${book.title}封面`" />
            <span>{{ book.title?.slice(0, 2) }}</span>
          </div>
          <div class="catalog-info">
            <span class="status" :class="book.status">{{ book.status === 'ON_SHELF' ? '上架' : '下架' }}</span>
            <h3>{{ book.title }}</h3>
            <p>{{ book.author }} · ISBN {{ book.isbn }}</p>
            <p>{{ book.summary }}</p>
          </div>
          <footer>
            <span>可借 {{ book.availableStock }} / {{ book.totalStock }}</span>
            <div class="book-actions">
              <template v-if="canManage">
                <button class="tiny-btn" type="button" @click="edit(book)">编辑</button>
                <button class="tiny-btn" type="button" @click="toggleStatus(book)">{{ book.status === 'ON_SHELF' ? '下架' : '上架' }}</button>
                <button class="tiny-btn danger-btn" type="button" @click="remove(book)">删除</button>
              </template>
              <button class="primary-btn" type="button" @click="borrow(book)" :disabled="book.availableStock < 1 || book.status !== 'ON_SHELF'">
                {{ canManage ? '为读者办理' : '借阅' }}
              </button>
            </div>
          </footer>
        </article>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon" aria-hidden="true">LF</div>
        <h3>暂无匹配图书</h3>
        <p>换一个关键词或分类试试。馆员也可以新增图书补充馆藏。</p>
        <button v-if="canManage" class="primary-btn" type="button" @click="editing = true">新增图书</button>
      </div>
    </section>
  </section>
  <dialog :open="editing" class="modal">
    <form class="modal-card" @submit.prevent="save">
      <h2>{{ form.id ? '编辑图书' : '新增图书' }}</h2>
      <div class="book-editor-grid">
        <label class="cover-uploader">
          <span>图书封面</span>
          <input type="file" accept="image/png,image/jpeg,image/webp" @change="uploadCover" />
          <div class="cover-preview" :class="{ 'has-image': form.coverUrl }">
            <img v-if="form.coverUrl" :src="assetUrl(form.coverUrl)" alt="" />
            <span v-else>上传封面</span>
          </div>
          <small>支持 JPG、PNG、WebP，最大 3MB</small>
        </label>
        <div class="book-editor-fields">
          <label>书名<input v-model="form.title" required /></label>
          <label>作者<input v-model="form.author" required /></label>
          <label>ISBN<input v-model="form.isbn" required /></label>
          <label>库存<input v-model.number="form.totalStock" type="number" min="0" required /></label>
        </div>
      </div>
      <label>简介<textarea v-model="form.summary" rows="3"></textarea></label>
      <div class="modal-actions">
        <button type="button" class="ghost-btn" @click="closeEditor">取消</button>
        <button type="submit" class="primary-btn">保存</button>
      </div>
    </form>
  </dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api/library'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const query = reactive({ keyword: '', categoryId: '', page: 1, size: 20 })
const books = ref([])
const categories = ref([])
const editing = ref(false)
const form = reactive({ title: '', author: '', isbn: '', totalStock: 1, availableStock: 1, summary: '', coverUrl: '', status: 'ON_SHELF' })
const canManage = computed(() => ['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role))
const availableTotal = computed(() => books.value.reduce((sum, book) => sum + Number(book.availableStock || 0), 0))
const palette = ['#eaf3ff', '#e8fff5', '#fff3e5', '#f4f0ff', '#eef2f8', '#e6f7ff']

function coverStyle(book) {
  return { '--cover': coverColor(book.id) }
}

function coverColor(id) {
  return palette[Number(id || 0) % palette.length]
}

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

function resetForm() {
  Object.assign(form, { id: undefined, title: '', author: '', isbn: '', totalStock: 1, availableStock: 1, summary: '', coverUrl: '', status: 'ON_SHELF' })
}

function edit(book) {
  Object.assign(form, book)
  editing.value = true
}

function closeEditor() {
  editing.value = false
  resetForm()
}

async function load() {
  const res = await api.books({ ...query, categoryId: query.categoryId || undefined })
  books.value = res.data.records
}

async function borrow(book) {
  const payload = { bookId: book.id }
  if (canManage.value) {
    const userId = window.prompt('请输入读者 ID 后办理借阅')
    if (!userId) return
    payload.userId = Number(userId)
  }
  await api.borrow(payload)
  await load()
}

async function uploadCover(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const result = await api.uploadCover(file)
  form.coverUrl = result.data.url
  event.target.value = ''
}

async function save() {
  if (!form.id) form.availableStock = form.totalStock
  await api.saveBook(form)
  closeEditor()
  await load()
}

async function toggleStatus(book) {
  await api.changeBookStatus(book.id, book.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF')
  await load()
}

async function remove(book) {
  if (!window.confirm(`确认删除《${book.title}》？`)) return
  await api.deleteBook(book.id)
  await load()
}

onMounted(async () => {
  categories.value = (await api.categories()).data
  await load()
})
</script>
