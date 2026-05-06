// В dev запросы идут на тот же хост; Vite проксирует /api → backend
// Без прокси: VITE_API_BASE=http://localhost:8080/api
const BASE_URL = import.meta.env.VITE_API_BASE ?? '/api'

const STORAGE_KEY = 'silverpear_session'

const giftRevealSeenKey = (userId) => `silverpear_gift_reveal_seen_${userId}`

export function readGiftRevealSeenSet(userId) {
  try {
    const raw = localStorage.getItem(giftRevealSeenKey(userId))
    if (!raw) return new Set()
    const arr = JSON.parse(raw)
    return new Set(Array.isArray(arr) ? arr.map(String) : [])
  } catch {
    return new Set()
  }
}

export function markGiftRevealSeen(userId, giftOrderId) {
  const s = readGiftRevealSeenSet(userId)
  s.add(String(giftOrderId))
  localStorage.setItem(giftRevealSeenKey(userId), JSON.stringify([...s]))
}

export function readStoredSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const s = JSON.parse(raw)
    if (!s?.token || !s?.userId) return null
    return s
  } catch {
    return null
  }
}

export function persistSession(session) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEY)
}

let authToken = readStoredSession()?.token ?? null

export function setAuthToken(token) {
  authToken = token
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers ?? {}) }
  if (authToken) headers.Authorization = `Bearer ${authToken}`

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      headers,
      ...options,
    })
  } catch (cause) {
    const err = new Error('NETWORK_ERROR')
    err.code = 'NETWORK_ERROR'
    err.cause = cause
    throw err
  }

  if (!response.ok) {
    const text = await response.text()
    let message = text || `HTTP ${response.status}`
    try {
      const j = JSON.parse(text)
      if (j.message) message = j.message
      if (j.errors?.length) {
        const uniqueErrors = [...new Set(j.errors.map((item) => String(item).trim()).filter(Boolean))]
        const normalizedMessage = String(message ?? '').trim().toLowerCase()
        const extras = uniqueErrors.filter((item) => {
          const normalizedItem = item.toLowerCase()
          return normalizedItem !== normalizedMessage && !normalizedMessage.includes(normalizedItem)
        })
        message = extras.length ? `${message}: ${extras.join('; ')}` : message
      }
    } catch {
      /* plain text */
    }
    const err = new Error(message)
    err.status = response.status
    err.path = path
    throw err
  }

  // POST/DELETE без тела: 204 или 200 с пустым телом — не вызываем response.json() (иначе Unexpected end of JSON input)
  if (response.status === 204 || response.status === 205) return null

  const text = await response.text()
  const trimmed = text.trim()
  if (!trimmed) return null

  try {
    return JSON.parse(trimmed)
  } catch {
    throw new Error('Некорректный ответ сервера (не JSON)')
  }
}

export const loginRequest = (login, password) =>
  request('/auth/login', { method: 'POST', body: JSON.stringify({ login, password }) })

export const registerRequest = (body) =>
  request('/auth/register', { method: 'POST', body: JSON.stringify(body) })

export const createGiftCardOrder = (body) =>
  request('/gift-cards', { method: 'POST', body: JSON.stringify(body) })

export const getReceivedGiftCards = () => request('/gift-cards/received')

export const getProducts = async () => {
  // Защита от перегруза: витрина берет первую страницу, а не весь огромный каталог.
  const page = await request('/products/page?page=0&size=60')
  return Array.isArray(page?.content) ? page.content : []
}
export const getProductById = (id) => request(`/products/${id}`)
export const searchProducts = ({ name, brand, category }) => {
  const params = new URLSearchParams()
  if (name) params.set('name', name)
  if (brand) params.set('brand', brand)
  if (category) params.set('category', category)
  return request(`/products/search?${params.toString()}`)
}
export const createProduct = (body) => request('/products', { method: 'POST', body: JSON.stringify(body) })
export const updateProduct = (id, body) =>
  request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(body) })
export const deleteProduct = (id) => request(`/products/${id}`, { method: 'DELETE' })

export const getUsers = () => request('/users')
export const createUser = (body) => request('/users', { method: 'POST', body: JSON.stringify(body) })
export const updateUser = (id, body) => request(`/users/${id}`, { method: 'PUT', body: JSON.stringify(body) })

export const patchUserProfile = (userId, body) =>
  request(`/users/${userId}/profile`, { method: 'PATCH', body: JSON.stringify(body) })
export const deleteUser = (id) => request(`/users/${id}`, { method: 'DELETE' })
export const getUserWithOrders = (userId) => request(`/users/${userId}/orders`)

export const getFavorites = (userId) => request(`/users/${userId}/favorites`)
export const addFavorite = (userId, productId) =>
  request(`/users/${userId}/favorites/${productId}`, { method: 'POST' })
export const removeFavorite = (userId, productId) =>
  request(`/users/${userId}/favorites/${productId}`, { method: 'DELETE' })
export const getFavoriteBrands = (userId) => request(`/users/${userId}/favorites/brands`)
export const addFavoriteBrand = (userId, brand) =>
  request(`/users/${userId}/favorites/brands?brand=${encodeURIComponent(brand)}`, { method: 'POST' })
export const removeFavoriteBrand = (userId, brand) =>
  request(`/users/${userId}/favorites/brands?brand=${encodeURIComponent(brand)}`, { method: 'DELETE' })

export const getAllOrders = () => request('/orders')
export const createOrder = (userId, body) =>
  request(`/orders/create?userId=${encodeURIComponent(String(userId))}`, { method: 'POST', body: JSON.stringify(body) })
export const deleteOrder = (id) => request(`/orders/${id}`, { method: 'DELETE' })
export const updateOrderStatus = (userId, orderId, status) =>
  request(`/orders/${userId}/user-orders/${orderId}?status=${status}`, { method: 'PATCH' })

export const startAsyncTask = () => request('/concurrency/tasks', { method: 'POST' })
export const getAsyncTaskStatus = (taskId) => request(`/concurrency/tasks/${taskId}`)
export const getRaceCondition = (threads, incrementsPerThread) =>
  request(`/concurrency/race-condition?threads=${threads}&incrementsPerThread=${incrementsPerThread}`)
