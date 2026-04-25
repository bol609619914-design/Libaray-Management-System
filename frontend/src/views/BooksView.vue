<template>
  <section class="toolbar" aria-label="图书筛选">
    <label>
      关键词
      <input v-model="query.keyword" placeholder="书名 / 作者 / ISBN" @keyup.enter="load" />
    </label>
    <label>
      分类
      <select v-model="query.categoryId">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
      </select>
    </label>
    <button class="primary-btn" type="button" @click="load">查询</button>
  </section>
  <section class="table-panel">
    <div class="section-heading">
      <h2>馆藏图书</h2>
      <button v-if="canManage" class="ghost-btn" type="button" @click="editing = true">新增图书</button>
    </div>
    <div class="book-grid">
      <article v-for="book in books" :key="book.id" class="book-card">
        <div>
          <span class="status" :class="book.status">{{ book.status === 'ON_SHELF' ? '上架' : '下架' }}</span>
          <h3>{{ book.title }}</h3>
          <p>{{ book.author }} · ISBN {{ book.isbn }}</p>
          <p>{{ book.summary }}</p>
        </div>
        <footer>
          <span>可借 {{ book.availableStock }} / {{ book.totalStock }}</span>
          <button class="primary-btn" type="button" @click="borrow(book)" :disabled="book.availableStock < 1">借阅</button>
        </footer>
      </article>
    </div>
  </section>
  <dialog :open="editing" class="modal">
    <form class="modal-card" @submit.prevent="save">
      <h2>新增图书</h2>
      <label>书名<input v-model="form.title" required /></label>
      <label>作者<input v-model="form.author" required /></label>
      <label>ISBN<input v-model="form.isbn" required /></label>
      <label>库存<input v-model.number="form.totalStock" type="number" min="0" required /></label>
      <label>简介<textarea v-model="form.summary" rows="3"></textarea></label>
      <div class="modal-actions">
        <button type="button" class="ghost-btn" @click="editing = false">取消</button>
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
const form = reactive({ title: '', author: '', isbn: '', totalStock: 1, availableStock: 1, summary: '', status: 'ON_SHELF' })
const canManage = computed(() => ['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role))

async function load() {
  const res = await api.books({ ...query, categoryId: query.categoryId || undefined })
  books.value = res.data.records
}

async function borrow(book) {
  await api.borrow({ bookId: book.id })
  await load()
}

async function save() {
  form.availableStock = form.totalStock
  await api.saveBook(form)
  editing.value = false
  await load()
}

onMounted(async () => {
  categories.value = (await api.categories()).data
  await load()
})
</script>
