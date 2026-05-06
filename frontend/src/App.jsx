import { useEffect, useMemo, useRef, useState } from 'react'
import logoImg from '../logo/image_1_1777227437574-removebg-preview.png'
import './App.css'
import {
  addFavorite,
  addFavoriteBrand,
  clearStoredSession,
  createOrder,
  createProduct,
  deleteOrder,
  deleteProduct,
  deleteUser,
  getAllOrders,
  getAsyncTaskStatus,
  getFavorites,
  getFavoriteBrands,
  getProductById,
  getProducts,
  getRaceCondition,
  getReceivedGiftCards,
  getUserWithOrders,
  getUsers,
  loginRequest,
  markGiftRevealSeen,
  persistSession,
  readGiftRevealSeenSet,
  readStoredSession,
  registerRequest,
  removeFavorite,
  removeFavoriteBrand,
  setAuthToken,
  startAsyncTask,
  updateProduct,
  updateOrderStatus,
  updateUser,
  patchUserProfile,
} from './api'
import { GiftCardWizard, giftImageUrlForDesignId } from './GiftCardWizard'
import { GiftRevealOverlay } from './GiftRevealOverlay'

const MAX_LOGIN_DIGITS = 15
const FREE_DELIVERY_THRESHOLD = 55
const DELIVERY_FEE = 7
const EXTRA_SPEC_OPTIONS = [
  'цвет',
  'тон',
  'основа',
  'гипоаллергенно',
  'spf',
  'финиш',
  'плотность',
  'область применения',
  'верхние ноты',
  'средние ноты',
  'базовые ноты',
]
const SKIN_TYPE_OPTIONS = ['для всех типов', 'сухая', 'жирная', 'комбинированная', 'чувствительная', 'нормальная']

function clampLoginInput(raw) {
  const value = String(raw ?? '')
  const trimmed = value.trimStart()
  const phoneLike = trimmed.startsWith('+') || /^\d/.test(trimmed)
  if (!phoneLike) {
    return value
  }
  let digitsLeft = MAX_LOGIN_DIGITS
  let out = ''
  for (const ch of value) {
    if (/\d/.test(ch)) {
      if (digitsLeft === 0) continue
      out += ch
      digitsLeft -= 1
      continue
    }
    out += ch
  }
  return out
}

function IconSearch(props) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
      <circle cx="11" cy="11" r="6" />
      <path d="M20 20l-4.3-4.3" strokeLinecap="round" />
    </svg>
  )
}

function IconHeart(props) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
      <path
        d="M12 20s-7-4.35-7-9.5A4.5 4.5 0 0 1 12 6a4.5 4.5 0 0 1 7 4.5C19 15.65 12 20 12 20z"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function IconUser(props) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
      <circle cx="12" cy="8" r="3.25" />
      <path d="M6.5 19.5c0-3 2.5-5 5.5-5s5.5 2 5.5 5" strokeLinecap="round" />
    </svg>
  )
}

function IconBag(props) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
      <path d="M8.5 9V6.5a3.5 3.5 0 0 1 7 0V9" strokeLinecap="round" />
      <path d="M6 9h12l-1 11H7L6 9z" strokeLinejoin="round" />
    </svg>
  )
}

/** Поиск по объединённым полям; при валидном RegExp — по шаблону (флаги iu), иначе подстрока без учёта регистра. */
function productMatchesSearch(product, query) {
  const q = query.trim()
  if (!q) return true
  const haystack = [
    product.name,
    product.brand,
    product.category,
    product.type,
    product.productType,
    product.gender,
    normalizeProductDescription(product.description),
  ]
    .map((x) => String(x ?? '').trim())
    .filter(Boolean)
    .join(' ')

  try {
    return new RegExp(q, 'iu').test(haystack)
  } catch {
    const normalizedHaystack = haystack.toLowerCase()
    return q
      .toLowerCase()
      .split(/\s+/)
      .filter(Boolean)
      .every((token) => normalizedHaystack.includes(token))
  }
}

function productImageSrc(product) {
  const u = product?.imageUrl
  if (u == null || typeof u !== 'string') return null
  const t = u.trim()
  return t || null
}

function formatProductVolumeMl(product) {
  const v = Number(product?.volume)
  if (!Number.isFinite(v) || v <= 0) return null
  return v % 1 === 0 ? `${Math.round(v)} мл` : `${v} мл`
}

function normalizeProductDescription(text) {
  if (text == null || text === '') return ''
  return String(text).replace(/\r\n/g, '\n').trim()
}

function extractMetaValue(text, key) {
  const normalized = normalizeProductDescription(text)
  if (!normalized) return null
  const escaped = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const match = normalized.match(new RegExp(`^${escaped}\\s*:\\s*(.+)$`, 'imu'))
  return match ? String(match[1]).trim() : null
}

function stripAdminMetaFromDescription(text) {
  const normalized = normalizeProductDescription(text)
  if (!normalized) return ''
  return normalized
    .split('\n')
    .filter((line) => !/^\s*(тип продукта|тип кожи|цвет|тон|основа|гипоаллергенно|spf|финиш|плотность|область применения|верхние ноты|средние ноты|базовые ноты|артикул)\s*:/iu.test(line))
    .join('\n')
    .trim()
}

function composeProductDescription(baseText, productType, skinType, extraSpecs = []) {
  const parts = []
  const cleanType = String(productType ?? '').trim()
  const cleanSkin = String(skinType ?? '').trim()
  const cleanBase = normalizeProductDescription(baseText)
  if (cleanType) parts.push(`тип продукта: ${cleanType}`)
  if (cleanSkin) parts.push(`тип кожи: ${cleanSkin}`)
  for (const spec of extraSpecs ?? []) {
    const key = String(spec?.key ?? '').trim()
    const value = String(spec?.value ?? '').trim()
    if (key && value) parts.push(`${key}: ${value}`)
  }
  if (cleanBase) parts.push(cleanBase)
  return parts.join('\n\n')
}

function extractDescriptionSpecs(text) {
  const normalized = normalizeProductDescription(text)
  if (!normalized) return []
  return normalized
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const idx = line.indexOf(':')
      if (idx <= 0) return null
      const key = line.slice(0, idx).trim()
      const value = line.slice(idx + 1).trim()
      if (!key || !value) return null
      return { key, value }
    })
    .filter(Boolean)
    .filter((spec) => !/^(тип продукта|тип кожи|артикул)$/iu.test(spec.key))
}

function prettifyErrorMessage(message) {
  const raw = String(message ?? '').trim()
  if (!raw) return ''
  const firstLine = raw
    .split('\n')
    .map((line) => line.trim())
    .find(Boolean)
  if (!firstLine) return ''
  const normalized = firstLine
    .replace(/^error\s*:\s*/iu, '')
    .replace(/^[a-z0-9_.]+\s*:\s*/iu, '')
    .trim()
  const text = normalized.toLowerCase()

  if (
    text.includes('network_error') ||
    text.includes('failed to fetch') ||
    text.includes('networkerror') ||
    text.includes('network request failed') ||
    text.includes('fetch failed')
  ) {
    return 'Нет связи с сервером. Проверьте интернет и попробуйте снова.'
  }
  if (/(bad credentials|invalid credentials|неверн\w*\s+(логин|парол))/iu.test(normalized)) {
    return 'Неверный логин или пароль.'
  }
  if (/(товар|product).*(не найден|not found)|(не найден).*(товар|product)/iu.test(normalized)) {
    return 'Товар не найден.'
  }
  if (/(пользователь|user).*(не найден|not found)|(не найден).*(пользователь|user)/iu.test(normalized)) {
    return 'Пользователь не найден.'
  }
  if (/\b401\b/.test(normalized)) return 'Сессия истекла. Войдите в аккаунт снова.'
  if (/\b403\b/.test(normalized)) return 'Недостаточно прав для этого действия.'
  if (/\b404\b/.test(normalized)) return 'Запрошенные данные не найдены.'
  if (/\b405\b/.test(normalized)) return 'Действие сейчас недоступно. Попробуйте позже.'
  if (/\b409\b/.test(normalized)) return 'Такое действие уже выполнено или данные конфликтуют.'
  if (/\b422\b/.test(normalized)) return 'Проверьте корректность введённых данных.'
  if (/\b5\d{2}\b/.test(normalized)) return 'Сервер временно недоступен. Попробуйте позже.'

  return normalized
}

const ORDER_STATUS_LABELS = {
  NEW: 'Новый',
  PROCESSED: 'В обработке',
  DELIVERED: 'Доставлен',
  TRANSFERRED_FOR_DELIVERY: 'Передан в доставку',
  EXPECTS: 'Ожидает',
  CANCELLED: 'Отменён',
}

const ADMIN_BRANDS_STORAGE_KEY = 'silverpear_admin_brands'

function formatOrderDate(value) {
  if (value == null || value === '') return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatGiftReceivedDate(value) {
  if (value == null || value === '') return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

function getDiscountPercent(product) {
  const directDiscount = Number(product?.discountPercent)
  if (Number.isFinite(directDiscount) && directDiscount > 0) return Math.round(directDiscount)
  const oldPrice = Number(product?.oldSalePrice)
  const salePrice = Number(product?.salePrice)
  if (!Number.isFinite(oldPrice) || !Number.isFinite(salePrice) || oldPrice <= salePrice || oldPrice <= 0) return 0
  return Math.round(((oldPrice - salePrice) / oldPrice) * 100)
}

function getDiscountBadgeVariant(discountPercent) {
  if (discountPercent >= 50) return 'hot'
  if (discountPercent >= 30) return 'pink'
  return 'yellow'
}

/** Остаток с бэка; без поля — не считаем «заканчивается». */
function getProductStock(product) {
  const n = Number(product?.stockQuantity)
  return Number.isFinite(n) ? n : null
}

/** Раздел «успей купить»: товары с остатком 5 и меньше (включая ноль). */
function isRunningOutStock(product) {
  const q = getProductStock(product)
  if (q == null) return false
  return q <= 5
}

function isLastUnit(product) {
  return getProductStock(product) === 1
}

function LastItemPlaque({ variant = 'tile' }) {
  return (
    <div
      className={`last-item-plaque ${variant === 'hero' ? 'last-item-plaque--hero' : ''}`}
      role="img"
      aria-label="Последний товар на складе"
    >
      <span className="last-item-plaque-mark" aria-hidden>
        1
      </span>
      <span className="last-item-plaque-copy">
        <span>ПОСЛЕДНИЙ</span>
        <span>ТОВАР</span>
      </span>
    </div>
  )
}

function RunningOutStockBadge({ stock }) {
  const qty = Number.isFinite(Number(stock)) ? Math.max(0, Math.trunc(Number(stock))) : 0
  const empty = qty <= 0
  return (
    <div className={`stock-badge ${empty ? 'stock-badge--empty' : ''}`} aria-label={`Остаток ${qty} штук`}>
      {qty} шт.
    </div>
  )
}

function initialsFromSession(user) {
  const rawS = (user?.surname ?? '').trim()
  const n = (user?.name ?? '').trim()
  const s = rawS === '—' || rawS === '-' ? '' : rawS
  const a = (s[0] ?? n[0] ?? '?').toUpperCase()
  const b = (s[0] && n[0] ? n[0] : n[1] ?? '').toUpperCase()
  return (a + b).slice(0, 2)
}

function displayPersonalFullName(src) {
  if (!src) return ''
  const parts = [src.surname, src.name, src.patronymic]
    .map((x) => (x != null ? String(x).trim() : ''))
    .filter((x) => x && x !== '—' && x !== '-')
  return parts.length ? parts.join(' ') : ''
}

const AVATAR_STORAGE_PREFIX = 'silverpear_avatar_'

function readStoredAvatarDataUrl(userId) {
  if (userId == null) return null
  try {
    const v = localStorage.getItem(`${AVATAR_STORAGE_PREFIX}${userId}`)
    return v && String(v).startsWith('data:image') ? v : null
  } catch {
    return null
  }
}

function persistAvatarDataUrl(userId, dataUrl) {
  if (userId == null) return
  try {
    if (dataUrl) localStorage.setItem(`${AVATAR_STORAGE_PREFIX}${userId}`, dataUrl)
    else localStorage.removeItem(`${AVATAR_STORAGE_PREFIX}${userId}`)
  } catch {
    /* quota */
  }
}

function shrinkImageFileToJpegDataUrl(file, maxSide = 400, quality = 0.86) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      URL.revokeObjectURL(url)
      try {
        let { width, height } = img
        const scale = Math.min(1, maxSide / Math.max(width, height, 1))
        const w = Math.max(1, Math.round(width * scale))
        const h = Math.max(1, Math.round(height * scale))
        const canvas = document.createElement('canvas')
        canvas.width = w
        canvas.height = h
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          reject(new Error('Не удалось обработать изображение'))
          return
        }
        ctx.drawImage(img, 0, 0, w, h)
        resolve(canvas.toDataURL('image/jpeg', quality))
      } catch (e) {
        reject(e instanceof Error ? e : new Error('Ошибка обработки фото'))
      }
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('Не удалось прочитать файл'))
    }
    img.src = url
  })
}

/** Подставляет цену/поля из каталога, если в избранном пришёл урезанный DTO. */
function mergeProductWithCatalog(product, catalogProducts) {
  if (!product || !Array.isArray(catalogProducts) || catalogProducts.length === 0) return product
  const c = catalogProducts.find((p) => String(p.id) === String(product.id))
  if (!c) return product
  const favPrice = Number(product.salePrice)
  const useFavPrice = Number.isFinite(favPrice) && favPrice > 0
  const pick = (favValue, catalogValue) => (favValue == null || favValue === '' ? catalogValue : favValue)
  return {
    ...product,
    ...c,
    id: pick(product.id, c.id),
    name: pick(product.name, c.name),
    brand: pick(product.brand, c.brand),
    category: pick(product.category, c.category),
    description: pick(product.description, c.description),
    inStock: pick(product.inStock, c.inStock),
    stockQuantity: pick(product.stockQuantity, c.stockQuantity),
    volume: pick(product.volume, c.volume),
    gender: pick(product.gender, c.gender),
    type: pick(product.type, c.type),
    salePrice: useFavPrice ? favPrice : Number(c.salePrice),
    imageUrl: pick(product.imageUrl, c.imageUrl),
  }
}

function favoriteFingerprint(product, catalogProducts) {
  const merged = mergeProductWithCatalog(product, catalogProducts ?? [])
  const norm = (value) =>
    String(value ?? '')
      .trim()
      .replace(/\u00A0/g, ' ')
      .replace(/\s+/g, ' ')
      .toLowerCase()
  const price = Number(merged?.salePrice)
  const priceKey = Number.isFinite(price) ? price.toFixed(2) : '0.00'
  return `${norm(merged?.name)}|${norm(merged?.brand)}|${norm(merged?.category)}|${priceKey}|${norm(merged?.imageUrl)}`
}

function normalizePromoCode(value) {
  return String(value ?? '')
    .trim()
    .replace(/\u00A0/g, ' ')
    .replace(/\s+/g, '')
    .toUpperCase()
}

function resolvePromoMeta(code) {
  const normalized = normalizePromoCode(code)
  if (normalized === 'МЕЧТА') return { code: normalized, discountPercent: 67, thankYou: false }
  if (normalized === 'ВЫСДАЛИЛАБУ') return { code: normalized, discountPercent: 99, thankYou: true }
  return null
}

function formatPriceByn(product) {
  const n = Number(product?.salePrice)
  if (!Number.isFinite(n)) return '— BYN'
  return `${n.toFixed(2)} BYN`
}

function resolvePurchasedProductPrice(product, catalogProducts) {
  const own = Number(product?.salePrice)
  if (Number.isFinite(own) && own > 0) return own
  const atPurchase = Number(product?._priceAtTime)
  if (Number.isFinite(atPurchase) && atPurchase > 0) return atPurchase
  const fromCatalog = Number(
    catalogProducts.find((p) => String(p.id) === String(product?.id))?.salePrice,
  )
  if (Number.isFinite(fromCatalog) && fromCatalog > 0) return fromCatalog
  return null
}

function normalizeProductsCatalog(items) {
  if (!Array.isArray(items)) return []
  const byName = new Map()
  for (const item of items) {
    if (!item || item.id == null) continue
    const key = String(item.name ?? '').trim().toLowerCase() || `id:${item.id}`
    const prev = byName.get(key)
    if (!prev || Number(item.id) < Number(prev.id)) byName.set(key, item)
  }
  return [...byName.values()].sort((a, b) => Number(a.id) - Number(b.id))
}

function FavoriteProductTile({ product, isFavorite, onOpenOverview, onToggleFavorite, onAddToCart, catalogProducts }) {
  const merged = mergeProductWithCatalog(product, catalogProducts ?? [])
  const coverSrc = productImageSrc(merged)
  const discountPercent = getDiscountPercent(merged)
  const hasDiscount = discountPercent > 0
  const discountVariant = getDiscountBadgeVariant(discountPercent)
  const priceLabel = formatPriceByn(merged)
  return (
    <article
      className="product-tile clickable-tile"
      role="button"
      tabIndex={0}
      onClick={() => onOpenOverview(merged)}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          onOpenOverview(merged)
        }
      }}
    >
      <div className={`product-visual${coverSrc ? ' product-visual--photo' : ''}`} aria-hidden>
        {coverSrc && <img className="product-visual-img" src={coverSrc} alt="" decoding="async" />}
        <button
          type="button"
          className="quick-cart-btn"
          title="Быстро добавить в корзину"
          onClick={(e) => {
            e.stopPropagation()
            onAddToCart(merged.id, 1)
          }}
        >
          <IconBag />
        </button>
        <button
          type="button"
          className={`icon-btn ${isFavorite ? 'active' : ''}`}
          title={isFavorite ? 'Убрать из избранного' : 'В избранное'}
          onClick={(e) => {
            e.stopPropagation()
            onToggleFavorite(product.id ?? merged.id, {
              forceRemove: isFavorite,
              sourceProduct: product,
            })
          }}
        >
          {isFavorite ? '❤' : '♡'}
        </button>
        {hasDiscount && (
          <>
            <span className="sale-ribbon">АКЦИЯ</span>
            <span className={`discount-chip ${discountVariant}`}>
              <span className="discount-chip-icon">⚡</span>
              <span className="discount-chip-value">-{discountPercent}%</span>
            </span>
          </>
        )}
      </div>
      <div className="product-body">
        <div className="product-brand">{merged.category ?? 'категория'}</div>
        <h3>{merged.name}</h3>
        <div className="price-row">
          <span className="price">{priceLabel}</span>
        </div>
      </div>
    </article>
  )
}

function App() {
  const [auth, setAuth] = useState(() => readStoredSession())
  const [authModal, setAuthModal] = useState(null)
  const [tab, setTab] = useState('catalog')
  const [users, setUsers] = useState([])
  const [adminPickUserId, setAdminPickUserId] = useState('')
  const [products, setProducts] = useState([])
  const [orders, setOrders] = useState([])
  const [favorites, setFavorites] = useState([])
  const [favoriteBrands, setFavoriteBrands] = useState([])
  const [userWithOrders, setUserWithOrders] = useState(null)
  const [cart, setCart] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [appliedSearch, setAppliedSearch] = useState('')
  const [productForm, setProductForm] = useState(initialProductForm)
  const [productExtraSpecs, setProductExtraSpecs] = useState([{ key: '', value: '' }])
  const [adminProductsPage, setAdminProductsPage] = useState(1)
  const [adminProductsQuery, setAdminProductsQuery] = useState('')
  const [adminUsersQuery, setAdminUsersQuery] = useState('')
  const [adminUsersPage, setAdminUsersPage] = useState(1)
  const [editingUserId, setEditingUserId] = useState(null)
  const [editingUserDraft, setEditingUserDraft] = useState(null)
  const [showEditUserPassword, setShowEditUserPassword] = useState(false)
  const [checkoutGiftInput, setCheckoutGiftInput] = useState('')
  const [promoInput, setPromoInput] = useState('')
  const [appliedPromo, setAppliedPromo] = useState(null)
  const [taskInfo, setTaskInfo] = useState(null)
  const [raceInfo, setRaceInfo] = useState(null)
  const [filterStatus, setFilterStatus] = useState('')
  const [headerSearchOpen, setHeaderSearchOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [logoAnimKey, setLogoAnimKey] = useState(0)
  const [activeNav, setActiveNav] = useState('catalog')
  const [brandQuery, setBrandQuery] = useState('')
  const [adminBrandInput, setAdminBrandInput] = useState('')
  const [adminOrderSort, setAdminOrderSort] = useState('newest')
  const [adminOrdersPage, setAdminOrdersPage] = useState(1)
  const [mainCategoryFilter, setMainCategoryFilter] = useState('all')
  const [mainOnlyInStock, setMainOnlyInStock] = useState(false)
  const [catalogPage, setCatalogPage] = useState(1)

  const [loginForm, setLoginForm] = useState({ login: '', password: '' })
  const [showLoginPassword, setShowLoginPassword] = useState(false)
  const [showRegisterPassword, setShowRegisterPassword] = useState(false)
  const [authError, setAuthError] = useState('')
  const [registerForm, setRegisterForm] = useState({
    phone: '+375 ',
    password: '',
    surname: '',
    name: '',
    patronymic: '',
  })
  const [profileDraft, setProfileDraft] = useState({ surname: '', name: '', patronymic: '' })
  const [profileSaving, setProfileSaving] = useState(false)
  const [avatarPreview, setAvatarPreview] = useState(null)
  const avatarInputRef = useRef(null)
  const [giftWizardOpen, setGiftWizardOpen] = useState(false)
  const [giftRevealQueue, setGiftRevealQueue] = useState([])
  const [receivedGiftCards, setReceivedGiftCards] = useState([])
  const [cartNotice, setCartNotice] = useState('')
  const [managedBrands, setManagedBrands] = useState(() => {
    try {
      const raw = localStorage.getItem(ADMIN_BRANDS_STORAGE_KEY)
      const parsed = raw ? JSON.parse(raw) : []
      return Array.isArray(parsed) ? parsed.map((x) => String(x).trim()).filter(Boolean) : []
    } catch {
      return []
    }
  })

  const searchInputRef = useRef(null)
  const cartBarRef = useRef(null)
  const giftHeroRef = useRef(null)
  const promoStripRef = useRef(null)

  const isAdmin = auth?.role === 'ADMIN'
  const activeUserId = auth?.userId

  useEffect(() => {
    localStorage.setItem(ADMIN_BRANDS_STORAGE_KEY, JSON.stringify(managedBrands))
  }, [managedBrands])

  useEffect(() => {
    if (auth?.token) setAuthToken(auth.token)
    else setAuthToken(null)
  }, [auth])

  const cartCount = useMemo(
    () => Object.values(cart).reduce((a, b) => a + b, 0),
    [cart],
  )

  const cartTotal = useMemo(() => {
    return Object.entries(cart).reduce((sum, [id, qty]) => {
      const p = products.find((x) => String(x.id) === String(id))
      return sum + (p ? p.salePrice * qty : 0)
    }, 0)
  }, [cart, products])

  const deliveryFee = useMemo(() => {
    if (cartTotal <= 0 || cartTotal >= FREE_DELIVERY_THRESHOLD) return 0
    return DELIVERY_FEE
  }, [cartTotal])

  const cartGrandTotal = useMemo(() => cartTotal + deliveryFee, [cartTotal, deliveryFee])
  const promoDiscountAmount = useMemo(() => {
    const percent = Number(appliedPromo?.discountPercent ?? 0)
    if (!Number.isFinite(percent) || percent <= 0) return 0
    return Math.round(cartGrandTotal * (percent / 100) * 100) / 100
  }, [cartGrandTotal, appliedPromo?.discountPercent])
  const payableBeforeGift = useMemo(
    () => Math.max(0, Math.round((cartGrandTotal - promoDiscountAmount) * 100) / 100),
    [cartGrandTotal, promoDiscountAmount],
  )

  /** Остаток по каждой карте: списание с баланса в порядке FIFO по дате получения. */
  const giftCardsWithRemaining = useMemo(() => {
    const list = Array.isArray(receivedGiftCards) ? receivedGiftCards : []
    if (!list.length) return []
    const sortedAsc = [...list].sort((a, b) => new Date(a.createdAt ?? 0) - new Date(b.createdAt ?? 0))
    const totalNominal = sortedAsc.reduce((s, g) => s + Number(g.amount ?? 0), 0)
    const balance = Number(userWithOrders?.giftBalance ?? 0)

    if (totalNominal <= 0) {
      return [...sortedAsc].reverse().map((g) => ({ ...g, remaining: 0 }))
    }

    if (balance >= totalNominal - 1e-6) {
      return [...sortedAsc]
        .reverse()
        .map((g) => ({ ...g, remaining: Math.max(0, Number(g.amount ?? 0)) }))
    }

    const spent = totalNominal - balance
    if (spent < 0) {
      const scale = balance / totalNominal
      return [...sortedAsc]
        .reverse()
        .map((g) => ({
          ...g,
          remaining: Math.round(Number(g.amount ?? 0) * scale * 100) / 100,
        }))
    }

    let toAllocate = spent
    const withRem = sortedAsc.map((g) => {
      const nom = Number(g.amount ?? 0)
      const take = Math.min(nom, toAllocate)
      toAllocate -= take
      return { ...g, remaining: Math.max(0, nom - take) }
    })
    return withRem.reverse()
  }, [receivedGiftCards, userWithOrders?.giftBalance])

  useEffect(() => {
    loadCatalog()
  }, [])

  useEffect(() => {
    if (!auth?.userId) {
      setGiftRevealQueue([])
      setReceivedGiftCards([])
      return
    }
    let cancelled = false
    ;(async () => {
      try {
        const list = await getReceivedGiftCards()
        if (cancelled) return
        const arr = Array.isArray(list) ? list : []
        const sorted = [...arr].sort((a, b) => new Date(b.createdAt ?? 0) - new Date(a.createdAt ?? 0))
        setReceivedGiftCards(sorted)
        const seen = readGiftRevealSeenSet(auth.userId)
        const unseen = sorted.filter((g) => g?.id != null && !seen.has(String(g.id)))
        setGiftRevealQueue(unseen)
      } catch {
        if (!cancelled) {
          setGiftRevealQueue([])
          setReceivedGiftCards([])
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [auth?.userId])

  useEffect(() => {
    if (activeNav !== 'gift' || !auth?.userId) return
    ;(async () => {
      try {
        await loadUserWithOrders(String(auth.userId))
      } catch {
        /* ignore */
      }
    })()
  }, [activeNav, auth?.userId])

  useEffect(() => {
    if (!auth) {
      setFavorites([])
      setFavoriteBrands([])
      setUserWithOrders(null)
      setOrders([])
      setUsers([])
      setAdminPickUserId('')
      return
    }
    ;(async () => {
      try {
        await loadFavorites(auth.userId)
        await loadFavoriteBrands(auth.userId)
        if (auth.role === 'ADMIN') {
          await loadAllOrders()
          await loadAdminUsers()
        }
      } catch (e) {
        if (String(e.message).includes('401') || String(e.message).includes('403')) {
          logout()
        } else setError(e.message)
      }
    })()
  }, [auth])

  useEffect(() => {
    if (!auth) return
    const uid = isAdmin && adminPickUserId ? adminPickUserId : String(auth.userId)
    ;(async () => {
      try {
        await loadUserWithOrders(uid)
      } catch (e) {
        setError(e.message)
      }
    })()
  }, [auth, isAdmin, adminPickUserId])

  useEffect(() => {
    if (!auth?.userId) return
    setProfileDraft({
      surname: String(userWithOrders?.surname ?? auth.surname ?? '').trim(),
      name: String(userWithOrders?.name ?? auth.name ?? '').trim(),
      patronymic: String(userWithOrders?.patronymic ?? auth.patronymic ?? '').trim(),
    })
  }, [auth?.userId, auth?.surname, auth?.name, auth?.patronymic, userWithOrders?.surname, userWithOrders?.name, userWithOrders?.patronymic])

  useEffect(() => {
    if (!auth?.userId) {
      setAvatarPreview(null)
      return
    }
    setAvatarPreview(readStoredAvatarDataUrl(auth.userId))
  }, [auth?.userId])

  async function loadAdminUsers() {
    try {
      const usersData = await getUsers()
      setUsers(usersData)
      if (usersData.length && !adminPickUserId) setAdminPickUserId(String(usersData[0].id))
    } catch (e) {
      setError(e.message)
    }
  }

  function setCartQty(productId, qty) {
    setCart((prev) => {
      const next = { ...prev }
      const n = Number(qty)
      if (!n || n < 1) delete next[productId]
      else next[productId] = n
      return next
    })
  }

  function addToCart(productId, delta = 1) {
    const product = products.find((x) => String(x.id) === String(productId))
    const stock = getProductStock(product)
    if (delta > 0 && stock != null) {
      const inCartNow = Number(cart[productId] ?? 0)
      if (stock <= 0 || inCartNow >= stock) {
        setCartNotice(`Товар "${product?.name ?? 'товар'}" закончился`)
        return
      }
    }
    setCart((prev) => {
      const cur = prev[productId] ?? 0
      let n = Math.max(0, cur + delta)
      if (delta > 0) {
        const p = products.find((x) => String(x.id) === String(productId))
        const q = getProductStock(p)
        if (q != null) n = Math.min(n, q)
      }
      const next = { ...prev }
      if (n < 1) delete next[productId]
      else next[productId] = n
      return next
    })
    if (delta > 0) {
      setCartNotice(`Товар "${product?.name ?? 'товар'}" добавлен в корзину`)
    }
  }

  useEffect(() => {
    if (!cartNotice) return
    const timer = setTimeout(() => setCartNotice(''), 3000)
    return () => clearTimeout(timer)
  }, [cartNotice])

  useEffect(() => {
    if (!error) return
    const timer = setTimeout(() => setError(''), 3000)
    return () => clearTimeout(timer)
  }, [error])

  useEffect(() => {
    if (cartCount > 0) return
    setPromoInput('')
    setAppliedPromo(null)
  }, [cartCount])

  useEffect(() => {
    setAdminProductsPage(1)
  }, [products.length])

  useEffect(() => {
    setAdminProductsPage(1)
  }, [adminProductsQuery])

  useEffect(() => {
    setAdminOrdersPage(1)
  }, [filterStatus, adminOrderSort, orders.length])

  useEffect(() => {
    setAdminUsersPage(1)
  }, [adminUsersQuery, users.length])

  async function loadCatalog() {
    try {
      setLoading(true)
      setError('')
      const productsData = await getProducts()
      setProducts(normalizeProductsCatalog(productsData))
    } catch (e) {
      setProducts([])
      setError('Не удалось загрузить товары из базы. Проверьте backend/API.')
    } finally {
      setLoading(false)
    }
  }

  async function loadFavorites(userId) {
    const data = await getFavorites(userId)
    const list = Array.isArray(data) ? data : data && typeof data === 'object' ? Object.values(data) : []
    const normalized = list.filter((item) => item && item.id != null)
    const missingDetails = normalized.filter((item) => {
      const hasCategory = String(item.category ?? '').trim().length > 0
      const hasPrice = Number(item.salePrice) > 0
      const hasImage = String(item.imageUrl ?? '').trim().length > 0
      return !hasCategory || !hasPrice || !hasImage
    })
    if (!missingDetails.length) {
      setFavorites(normalized)
      return
    }

    const byId = new Map(normalized.map((item) => [String(item.id), item]))
    const resolved = await Promise.allSettled(
      missingDetails.map(async (item) => {
        try {
          const fresh = await getProductById(item.id)
          return { kind: 'ok', id: item.id, product: fresh }
        } catch (e) {
          const status = Number(e?.status)
          if (status === 404 || status === 410) return { kind: 'stale', id: item.id }
          return { kind: 'keep', id: item.id }
        }
      }),
    )

    const staleIds = []
    for (const entry of resolved) {
      if (entry.status !== 'fulfilled') continue
      const result = entry.value
      const key = String(result.id)
      if (result.kind === 'ok' && result.product) {
        byId.set(key, { ...byId.get(key), ...result.product })
      } else if (result.kind === 'stale') {
        staleIds.push(result.id)
        byId.delete(key)
      }
    }

    if (staleIds.length && auth?.userId) {
      await Promise.allSettled(staleIds.map((id) => removeFavorite(auth.userId, id)))
    }
    setFavorites([...byId.values()])
  }

  async function loadFavoriteBrands(userId) {
    const items = await getFavoriteBrands(userId)
    const list = Array.isArray(items) ? items : items && typeof items === 'object' ? Object.values(items) : []
    setFavoriteBrands(list)
  }

  async function loadUserWithOrders(userId) {
    try {
      setUserWithOrders(await getUserWithOrders(userId))
    } catch (e) {
      setError(e.message)
    }
  }

  async function loadAllOrders() {
    try {
      setOrders(await getAllOrders())
    } catch (e) {
      setError(e.message)
    }
  }

  function onSearchProducts(event) {
    event.preventDefault()
    setAppliedSearch(searchInput.trim())
    setHeaderSearchOpen(false)
  }

  function applySearchPreset(value) {
    setSearchInput(value)
    setAppliedSearch(value)
    goCatalogHome()
    setHeaderSearchOpen(false)
  }

  function onBrandsSearch(event) {
    event.preventDefault()
    const value = brandQuery.trim()
    if (!value) return
    applySearchPreset(value)
  }

  async function onSaveProduct(event) {
    event.preventDefault()
    try {
      const preparedDescription = composeProductDescription(
        productForm.description,
        productForm.productType,
        productForm.skinType,
        productExtraSpecs,
      )
      const payload = {
        ...productForm,
        description: preparedDescription,
        salePrice: Number(productForm.salePrice || 0),
        volume: Number(productForm.volume || 0),
        stockQuantity: Number(productForm.stockQuantity || 0),
      }
      if (productForm.id) await updateProduct(productForm.id, payload)
      else await createProduct(payload)
      setProductForm(initialProductForm)
      setProductExtraSpecs([{ key: '', value: '' }])
      setProducts(normalizeProductsCatalog(await getProducts()))
    } catch (e) {
      setError(e.message)
    }
  }

  async function onDeleteProduct(id) {
    if (!window.confirm(`Подтвердите удаление: товар #${id}`)) return
    try {
      await deleteProduct(id)
      setProducts(normalizeProductsCatalog(await getProducts()))
      setCart((prev) => {
        const next = { ...prev }
        delete next[id]
        return next
      })
    } catch (e) {
      setError(e.message)
    }
  }

  function requireAuth(action) {
    if (!auth) {
      setError('Войдите в аккаунт, чтобы ' + action)
      setAuthModal('login')
      return false
    }
    return true
  }

  function scrollToRef(ref) {
    ref.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }

  function onUtilitySearch() {
    if (headerSearchOpen) {
      closeSearchPanel()
      return
    }
    setActiveNav('catalog')
    setSelectedProduct(null)
    setTab('catalog')
    setSearchInput(appliedSearch)
    setHeaderSearchOpen(true)
    requestAnimationFrame(() => searchInputRef.current?.focus())
  }

  function closeSearchPanel() {
    setHeaderSearchOpen(false)
    setSearchInput('')
    setAppliedSearch('')
  }

  function goBrands() {
    setActiveNav('brands')
    setTab('brands')
    setHeaderSearchOpen(false)
  }

  function goGiftCards() {
    setActiveNav('gift')
    setTab('catalog')
    requestAnimationFrame(() => scrollToRef(giftHeroRef))
  }

  function openGiftWizard() {
    if (!requireAuth('оформить подарочную карту')) return
    setGiftWizardOpen(true)
  }

  function goCatalogHome(nextNav = 'catalog') {
    setActiveNav(nextNav)
    setSelectedProduct(null)
    setTab('catalog')
    setCatalogPage(1)
    if (nextNav === 'discount') {
      setMainCategoryFilter('all')
      setMainOnlyInStock(false)
    }
  }

  function onLogoClick() {
    setHeaderSearchOpen(false)
    setAuthModal(null)
    setGiftWizardOpen(false)
    goCatalogHome('catalog')
    setLogoAnimKey((prev) => prev + 1)
  }

  function onUtilityFavorites() {
    if (!requireAuth('работать с избранным')) return
    setActiveNav('favorites')
    setSelectedProduct(null)
    setTab('favorites')
  }

  function onUtilityProfile() {
    setHeaderSearchOpen(false)
    if (!auth) {
      setAuthModal('login')
      return
    }
    setSelectedProduct(null)
    setAuthModal(null)
    setActiveNav('account')
    setTab('account')
  }

  function onUtilityCart() {
    setActiveNav('')
    setSelectedProduct(null)
    setTab('cart')
  }

  function openProductOverview(product) {
    setSelectedProduct(mergeProductWithCatalog(product, products))
    if (tab !== 'catalog') {
      setTab('catalog')
      setActiveNav('catalog')
    }
  }

  function closeProductOverview() {
    setSelectedProduct(null)
  }

  function isStaleSessionError(message) {
    const text = String(message ?? '')
    return (
      text.includes('User not found') ||
      text.includes('Пользователь не найден') ||
      text.includes('401') ||
      text.includes('Нет доступа')
    )
  }

  async function onToggleFavorite(productId, options = {}) {
    if (!requireAuth('добавлять в избранное')) return
    if (productId == null || productId === '') {
      setError('Не удалось добавить в избранное: товар отсутствует в текущем каталоге.')
      return
    }
    const { forceRemove = false, sourceProduct = null } = options ?? {}
    const idStr = String(productId)
    const existsById = favorites.some((item) => String(item.id) === idStr)
    const existsByFingerprint = sourceProduct
      ? favorites.some((item) => favoriteFingerprint(item, products) === favoriteFingerprint(sourceProduct, products))
      : false
    const exists = forceRemove ? (existsById || existsByFingerprint) : existsById
    const snapshot = favorites.map((x) => x)

    if (exists) {
      const clicked =
        favorites.find((item) => String(item.id) === idStr) ??
        (sourceProduct
          ? favorites.find((item) => favoriteFingerprint(item, products) === favoriteFingerprint(sourceProduct, products))
          : null)
      const fp = clicked ? favoriteFingerprint(clicked, products) : null
      const idsToRemove = fp
        ? favorites
          .filter((item) => favoriteFingerprint(item, products) === fp)
          .map((item) => item.id)
        : [productId]
      const uniqueIdsToRemove = [...new Set(idsToRemove.map((id) => String(id)))]
      const idSet = new Set(uniqueIdsToRemove)
      setFavorites((prev) => prev.filter((item) => !idSet.has(String(item.id))))
      try {
        await Promise.all(uniqueIdsToRemove.map((id) => removeFavorite(auth.userId, id)))
        await loadFavorites(auth.userId)
      } catch (e) {
        setFavorites(snapshot)
        if (isStaleSessionError(e.message)) {
          logout()
          setError('Сессия устарела после перезапуска сервера. Войдите снова.')
          return
        }
        setError(e.message)
      }
      return
    } else {
      const toAdd =
        products.find((p) => String(p.id) === idStr) ??
        (selectedProduct && String(selectedProduct.id) === idStr ? selectedProduct : null) ??
        { id: productId, name: 'Товар', brand: '', category: '', salePrice: 0 }
      setFavorites((prev) => (prev.some((x) => String(x.id) === idStr) ? prev : [...prev, toAdd]))
    }

    try {
      await addFavorite(auth.userId, productId)
      await loadFavorites(auth.userId)
    } catch (e) {
      setFavorites(snapshot)
      if (isStaleSessionError(e.message)) {
        logout()
        setError('Сессия устарела после перезапуска сервера. Войдите снова.')
        return
      }
      setError(e.message)
    }
  }

  async function onToggleFavoriteBrand(brand) {
    if (!requireAuth('добавлять бренд в избранное')) return
    try {
      const exists = favoriteBrands.some((item) => item.toLowerCase() === brand.toLowerCase())
      if (exists) await removeFavoriteBrand(auth.userId, brand)
      else await addFavoriteBrand(auth.userId, brand)
      await loadFavoriteBrands(auth.userId)
    } catch (e) {
      if (isStaleSessionError(e.message)) {
        logout()
        setError('Сессия устарела после перезапуска сервера. Войдите снова.')
        return
      }
      setError(e.message)
    }
  }

  function onAddManagedBrand(event) {
    event.preventDefault()
    const value = adminBrandInput.trim()
    if (!value) return
    setManagedBrands((prev) => (prev.some((x) => x.toLowerCase() === value.toLowerCase()) ? prev : [...prev, value]))
    setAdminBrandInput('')
  }

  function confirmDeleteAction(label) {
    return window.confirm(`Подтвердите удаление: ${label}`)
  }

  function onRemoveManagedBrand(brand) {
    if (!confirmDeleteAction(`бренд «${brand}»`)) return
    setManagedBrands((prev) => prev.filter((x) => x.toLowerCase() !== String(brand).toLowerCase()))
  }

  function onAddExtraSpec() {
    setProductExtraSpecs((prev) => [...prev, { key: '', value: '' }])
  }

  function onUpdateExtraSpec(idx, patch) {
    setProductExtraSpecs((prev) => prev.map((item, i) => (i === idx ? { ...item, ...patch } : item)))
  }

  function onRemoveExtraSpec(idx) {
    if (!confirmDeleteAction('характеристику')) return
    setProductExtraSpecs((prev) => {
      const next = prev.filter((_, i) => i !== idx)
      return next.length ? next : [{ key: '', value: '' }]
    })
  }

  function onAdminProductImagePick(file) {
    if (!file) return
    setProductForm((p) => ({ ...p, imageUrl: `/products/${file.name}` }))
  }

  async function onAdminUpdateOrderStatus(order, status) {
    const userId = order?.user?.id ?? order?.userId
    if (!userId) {
      setError('Не удалось определить пользователя заказа для смены статуса')
      return
    }
    try {
      await updateOrderStatus(userId, order.id, status)
      await loadAllOrders()
    } catch (e) {
      setError(e.message)
    }
  }

  function onStartEditUser(user) {
    setEditingUserId(user.id)
    setShowEditUserPassword(false)
    setEditingUserDraft({
      id: user.id,
      login: user.login ?? '',
      password: '',
      name: user.name ?? '',
      surname: user.surname ?? '',
      patronymic: user.patronymic ?? '',
      email: '',
      phone: user.phone ?? user.login ?? '',
    })
  }

  function onCancelEditUser() {
    setEditingUserId(null)
    setEditingUserDraft(null)
    setShowEditUserPassword(false)
  }

  async function onSaveEditedUser() {
    if (!editingUserId || !editingUserDraft) return
    if (!String(editingUserDraft.password ?? '').trim()) {
      setError('Укажите пароль для сохранения пользователя')
      return
    }
    try {
      const normalizedLogin = String(editingUserDraft.login ?? '').trim()
      await updateUser(editingUserId, {
        ...editingUserDraft,
        login: normalizedLogin,
        phone: normalizedLogin,
      })
      onCancelEditUser()
      await loadAdminUsers()
    } catch (e) {
      setError(e.message)
    }
  }

  async function onDeleteUser(id) {
    if (!confirmDeleteAction(`пользователя #${id}`)) return
    try {
      await deleteUser(id)
      await loadAdminUsers()
      if (String(adminPickUserId) === String(id)) setAdminPickUserId('')
    } catch (e) {
      setError(e.message)
    }
  }

  async function submitOrder(productQuantities, options = {}) {
    if (!auth) throw new Error('Войдите, чтобы оформить заказ')
    if (!Object.keys(productQuantities).length) throw new Error('Добавьте товары в заказ')
    const uid = Number(auth.userId)
    if (!Number.isFinite(uid)) throw new Error('Сессия пользователя некорректна, войдите снова')
    const payload = { productQuantities }
    const g = options.giftCardAmount
    if (g != null && Number(g) > 0) payload.giftCardAmount = Number(g)
    await createOrder(uid, payload)
    if (isAdmin) await loadAllOrders()
    const profileId = isAdmin && adminPickUserId ? adminPickUserId : String(auth.userId)
    await loadUserWithOrders(profileId)
  }

  async function goToCheckout() {
    if (!requireAuth('оформить заказ')) return
    if (!Object.keys(cart).length) {
      setError('Добавьте товары в корзину')
      return
    }
    try {
      await loadUserWithOrders(String(auth.userId))
    } catch {
      /* профиль подтянется при оформлении */
    }
    setCheckoutGiftInput('')
    setTab('checkout')
  }

  async function onConfirmCheckout() {
    if (!requireAuth('оформить заказ')) return
    if (!Object.keys(cart).length) {
      setError('Корзина пуста')
      setTab('cart')
      return
    }
    try {
      const cartSnapshot = Object.entries(cart).map(([id, qty]) => [String(id), Number(qty)])
      const productQuantities = {}
      Object.entries(cart).forEach(([id, qty]) => {
        productQuantities[id] = Number(qty)
      })
      const raw = String(checkoutGiftInput ?? '').replace(',', '.').trim()
      let gift = raw === '' ? 0 : Number(raw)
      if (!Number.isFinite(gift) || gift < 0) gift = 0
      const balance = Number(userWithOrders?.giftBalance ?? 0)
      const maxApply = Math.min(balance, payableBeforeGift)
      gift = Math.min(gift, maxApply)
      await submitOrder(productQuantities, { giftCardAmount: gift > 0 ? gift : undefined })
      setProducts((prev) =>
        prev.map((p) => {
          const hit = cartSnapshot.find(([id]) => String(p.id) === id)
          if (!hit) return p
          const dec = Number(hit[1] ?? 0)
          const current = getProductStock(p)
          if (current == null) return p
          const nextStock = Math.max(0, current - dec)
          return { ...p, stockQuantity: nextStock, inStock: nextStock > 0 }
        }),
      )
      setCart({})
      setCheckoutGiftInput('')
      setPromoInput('')
      setAppliedPromo(null)
      setTab('orders')
    } catch (e) {
      setError(e.message)
    }
  }

  function onApplyPromoCode(event) {
    event.preventDefault()
    const meta = resolvePromoMeta(promoInput)
    if (!meta) {
      setAppliedPromo(null)
      return
    }
    setAppliedPromo(meta)
  }

  async function onDeleteOrder(id) {
    if (!confirmDeleteAction(`заказ #${id}`)) return
    try {
      await deleteOrder(id)
      if (isAdmin) await loadAllOrders()
      await loadUserWithOrders(auth.userId)
    } catch (e) {
      setError(e.message)
    }
  }

  async function onStartAsyncTask() {
    try {
      const started = await startAsyncTask()
      setTaskInfo({ ...started, state: 'ACCEPTED', progressPercent: 0 })
      let active = true
      while (active) {
        const status = await getAsyncTaskStatus(started.taskId)
        setTaskInfo(status)
        if (status.state === 'COMPLETED' || status.state === 'FAILED') active = false
        else await wait(1000)
      }
    } catch (e) {
      setError(e.message)
    }
  }

  async function onRaceCondition() {
    try {
      setRaceInfo(await getRaceCondition(80, 12000))
    } catch (e) {
      setError(e.message)
    }
  }

  async function onLogin(e) {
    e.preventDefault()
    try {
      setAuthError('')
      const login = loginForm.login.trim().replace(/\u00A0/g, ' ')
      const data = await loginRequest(login, loginForm.password)
      const session = {
        token: data.token,
        userId: data.userId,
        login: data.login,
        name: data.name,
        surname: data.surname,
        patronymic: data.patronymic ?? '',
        role: data.role,
      }
      persistSession(session)
      setAuthToken(data.token)
      setAuth(session)
      setAuthModal(null)
      setLoginForm({ login: '', password: '' })
    } catch (err) {
      setAuthError(prettifyErrorMessage(err?.message))
    }
  }

  async function onRegister(e) {
    e.preventDefault()
    try {
      setAuthError('')
      const data = await registerRequest({
        phone: registerForm.phone.trim().replace(/\u00A0/g, ' '),
        password: registerForm.password,
        surname: registerForm.surname.trim().replace(/\u00A0/g, ' '),
        name: registerForm.name.trim().replace(/\u00A0/g, ' '),
        patronymic: registerForm.patronymic.trim().replace(/\u00A0/g, ' '),
      })
      const session = {
        token: data.token,
        userId: data.userId,
        login: data.login,
        name: data.name,
        surname: data.surname,
        patronymic: data.patronymic ?? '',
        role: data.role,
      }
      persistSession(session)
      setAuthToken(data.token)
      setAuth(session)
      setAuthModal(null)
      setRegisterForm({ phone: '+375 ', password: '', surname: '', name: '', patronymic: '' })
    } catch (err) {
      setAuthError(prettifyErrorMessage(err?.message))
    }
  }

  async function onSaveProfile(e) {
    e.preventDefault()
    if (!auth?.userId) return
    setProfileSaving(true)
    setError('')
    try {
      const updated = await patchUserProfile(auth.userId, {
        surname: profileDraft.surname.trim(),
        name: profileDraft.name.trim(),
        patronymic: profileDraft.patronymic.trim(),
      })
      const session = {
        ...auth,
        name: updated.name,
        surname: updated.surname,
        patronymic: updated.patronymic ?? '',
      }
      persistSession(session)
      setAuth(session)
      await loadUserWithOrders(String(auth.userId))
    } catch (err) {
      setError(err.message)
    } finally {
      setProfileSaving(false)
    }
  }

  async function onAvatarFileChange(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file || !auth?.userId) return
    if (!file.type.startsWith('image/')) {
      setError('Выберите файл изображения (JPEG, PNG, WebP или GIF).')
      return
    }
    if (file.size > 8 * 1024 * 1024) {
      setError('Размер файла не больше 8 МБ.')
      return
    }
    setError('')
    try {
      const dataUrl = await shrinkImageFileToJpegDataUrl(file, 400, 0.86)
      persistAvatarDataUrl(auth.userId, dataUrl)
      setAvatarPreview(dataUrl)
    } catch (err) {
      setError(err.message || 'Не удалось сохранить фото')
    }
  }

  function clearAvatarPhoto() {
    if (!auth?.userId) return
    persistAvatarDataUrl(auth.userId, null)
    setAvatarPreview(null)
  }

  function logout() {
    clearStoredSession()
    setAuthToken(null)
    setAuth(null)
    setAuthModal(null)
    setAuthError('')
    setGiftRevealQueue([])
  }

  function dismissGiftReveal() {
    const current = giftRevealQueue[0]
    if (!current?.id || !auth?.userId) return
    markGiftRevealSeen(auth.userId, current.id)
    setGiftRevealQueue((q) => q.slice(1))
  }

  const catalogSearchQuery = headerSearchOpen ? searchInput.trim() : appliedSearch.trim()

  const productsFiltered = useMemo(() => {
    const q = catalogSearchQuery
    if (!q) return products
    return products.filter((p) => productMatchesSearch(p, q))
  }, [products, catalogSearchQuery])

  const productsCatalogList = useMemo(() => {
    let base = productsFiltered
    if (activeNav === 'discount') {
      base = base.filter(isRunningOutStock)
    }
    if (mainCategoryFilter !== 'all') {
      const selected = mainCategoryFilter.toLowerCase()
      base = base.filter((p) => String(p.category ?? '').toLowerCase() === selected)
    }
    if (mainOnlyInStock) {
      base = base.filter((p) => p.inStock)
    }
    return base
  }, [productsFiltered, activeNav, mainCategoryFilter, mainOnlyInStock])

  const mainCatalogCategories = useMemo(() => {
    const set = new Set()
    products.forEach((p) => {
      const c = String(p.category ?? '').trim()
      if (c) set.add(c)
    })
    return ['all', ...Array.from(set).sort((a, b) => a.localeCompare(b, 'ru'))]
  }, [products])

  const CATALOG_PAGE_SIZE = 6
  const catalogTotalPages = Math.max(1, Math.ceil(productsCatalogList.length / CATALOG_PAGE_SIZE))
  const productsCatalogPage = useMemo(() => {
    const safePage = Math.min(Math.max(1, catalogPage), catalogTotalPages)
    const start = (safePage - 1) * CATALOG_PAGE_SIZE
    return productsCatalogList.slice(start, start + CATALOG_PAGE_SIZE)
  }, [productsCatalogList, catalogPage, catalogTotalPages])

  useEffect(() => {
    setCatalogPage(1)
  }, [activeNav, catalogSearchQuery, mainCategoryFilter, mainOnlyInStock])

  const ordersToShow = filterStatus ? orders.filter((item) => item.status === filterStatus) : orders
  const adminOrdersSorted = useMemo(() => {
    const list = [...ordersToShow]
    list.sort((a, b) => {
      const ta = new Date(a.orderDate ?? 0).getTime()
      const tb = new Date(b.orderDate ?? 0).getTime()
      return adminOrderSort === 'oldest' ? ta - tb : tb - ta
    })
    return list
  }, [ordersToShow, adminOrderSort])
  const ADMIN_ORDERS_PAGE_SIZE = 6
  const adminOrdersTotalPages = Math.max(1, Math.ceil(adminOrdersSorted.length / ADMIN_ORDERS_PAGE_SIZE))
  const adminOrdersPageItems = useMemo(() => {
    const safePage = Math.min(Math.max(1, adminOrdersPage), adminOrdersTotalPages)
    const start = (safePage - 1) * ADMIN_ORDERS_PAGE_SIZE
    return adminOrdersSorted.slice(start, start + ADMIN_ORDERS_PAGE_SIZE)
  }, [adminOrdersSorted, adminOrdersPage, adminOrdersTotalPages])

  const orderHistorySorted = useMemo(() => {
    const list = userWithOrders?.orders ?? []
    return [...list].sort((a, b) => {
      const ta = new Date(a.orderDate ?? 0).getTime()
      const tb = new Date(b.orderDate ?? 0).getTime()
      return tb - ta
    })
  }, [userWithOrders])

  /** Уникальные товары из истории заказов (сначала недавние покупки). */
  const purchasedProducts = useMemo(() => {
    const seen = new Set()
    const out = []
    for (const order of orderHistorySorted ?? []) {
      for (const item of order.orderItems ?? []) {
        const p = item.product
        const id = p?.id != null ? String(p.id) : null
        if (!id || seen.has(id)) continue
        seen.add(id)
        out.push({
          ...p,
          _priceAtTime: item.priceAtTime,
          _purchaseMeta: `${formatOrderDate(order.orderDate)} · ${item.quantity} шт.`,
        })
      }
    }

    // Добавляем незавершенные текущие позиции из корзины, чтобы блок не был пустым до оформления.
    for (const [id, qty] of Object.entries(cart)) {
      const pid = String(id)
      if (seen.has(pid)) continue
      const p = products.find((x) => String(x.id) === pid)
      if (!p) continue
      seen.add(pid)
      out.push({
        ...p,
        _purchaseMeta: `незавершенный заказ · ${qty} шт.`,
      })
    }
    return out
  }, [orderHistorySorted, cart, products])

  const selectedOverview = selectedProduct ? buildProductOverview(selectedProduct) : null
  const similarProducts = useMemo(() => {
    if (!selectedProduct) return []
    return products.filter((p) => String(p.id) !== String(selectedProduct.id)).slice(0, 4)
  }, [products, selectedProduct])
  const allKnownBrands = useMemo(() => {
    const fromProducts = products.map((item) => String(item.brand ?? '').trim()).filter(Boolean)
    return [...new Set(fromProducts)].sort((a, b) => a.localeCompare(b, 'ru'))
  }, [products])
  const adminBrandOptions = useMemo(() => {
    const set = new Set(allKnownBrands)
    const current = String(productForm.brand ?? '').trim()
    if (current) set.add(current)
    return [...set].sort((a, b) => a.localeCompare(b, 'ru'))
  }, [allKnownBrands, productForm.brand])
  const ADMIN_PRODUCTS_PAGE_SIZE = 8
  const adminProductsFiltered = useMemo(() => {
    const q = adminProductsQuery.trim().toLowerCase()
    if (!q) return products
    return products.filter((product) => {
      const haystack = [
        String(product.id ?? ''),
        String(product.name ?? ''),
        String(product.brand ?? ''),
        String(product.category ?? ''),
      ]
        .join(' ')
        .toLowerCase()
      return haystack.includes(q)
    })
  }, [products, adminProductsQuery])
  const adminProductsTotalPages = Math.max(1, Math.ceil(adminProductsFiltered.length / ADMIN_PRODUCTS_PAGE_SIZE))
  const adminProductsPageItems = useMemo(() => {
    const safePage = Math.min(Math.max(1, adminProductsPage), adminProductsTotalPages)
    const start = (safePage - 1) * ADMIN_PRODUCTS_PAGE_SIZE
    return adminProductsFiltered.slice(start, start + ADMIN_PRODUCTS_PAGE_SIZE)
  }, [adminProductsFiltered, adminProductsPage, adminProductsTotalPages])
  const adminUsersFiltered = useMemo(() => {
    const q = adminUsersQuery.trim().toLowerCase()
    const nonAdminUsers = users.filter((user) => String(user?.id ?? '') !== String(auth?.userId ?? ''))
    if (!q) return nonAdminUsers
    return nonAdminUsers.filter((user) => {
      const fullName = [user?.surname, user?.name, user?.patronymic].filter(Boolean).join(' ')
      const haystack = [String(user?.id ?? ''), String(user?.login ?? ''), fullName, String(user?.phone ?? '')]
        .join(' ')
        .toLowerCase()
      return haystack.includes(q)
    })
  }, [users, adminUsersQuery, auth?.userId])
  const ADMIN_USERS_PAGE_SIZE = 5
  const adminUsersTotalPages = Math.max(1, Math.ceil(adminUsersFiltered.length / ADMIN_USERS_PAGE_SIZE))
  const adminUsersPageItems = useMemo(() => {
    const safePage = Math.min(Math.max(1, adminUsersPage), adminUsersTotalPages)
    const start = (safePage - 1) * ADMIN_USERS_PAGE_SIZE
    return adminUsersFiltered.slice(start, start + ADMIN_USERS_PAGE_SIZE)
  }, [adminUsersFiltered, adminUsersPage, adminUsersTotalPages])

  const brandSections = useMemo(() => buildBrandSections(allKnownBrands), [allKnownBrands])
  const brandSectionsFiltered = useMemo(() => {
    const q = brandQuery.trim().toLowerCase()
    if (!q) return brandSections
    return brandSections
      .map((section) => ({
      ...section,
      items: section.items.filter((item) => item.toLowerCase().includes(q)),
      }))
      .filter((section) => section.items.length > 0)
  }, [brandQuery, brandSections])
  const errorToastText = useMemo(() => prettifyErrorMessage(error), [error])

  const brandsIndex = useMemo(() => brandSections.map((section) => section.title), [brandSections])
  const favoriteBrandSet = useMemo(() => new Set(favoriteBrands.map((item) => item.toLowerCase())), [favoriteBrands])

  function jumpToBrandLetter(letter) {
    const sectionId = `brand-section-${encodeURIComponent(letter)}`
    const target = document.getElementById(sectionId)
    if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className="app-shell">
      <header className="ga-header">
        <div className="ga-header-top">
          <div className="ga-header-side ga-header-left">
            <button type="button" className="ga-city-btn" aria-label="Город">
              <span className="ga-city-caret" aria-hidden>
                ▼
              </span>
              Минск
            </button>
          </div>

          <div className="ga-header-center">
            <button type="button" className="ga-logo-block" onClick={onLogoClick} aria-label="На главную">
              <img
                key={logoAnimKey}
                src={logoImg}
                alt=""
                className="ga-logo-img logo-wobble-once"
                width={62}
                height={62}
                decoding="async"
              />
            </button>
          </div>

          <div className="ga-header-side ga-header-right">
            <div className="ga-header-icons">
              <button
                type="button"
                className="ga-icon-btn"
                title={headerSearchOpen ? 'Закрыть поиск' : 'Поиск'}
                aria-label={headerSearchOpen ? 'Закрыть поиск' : 'Поиск'}
                onClick={onUtilitySearch}
              >
                {headerSearchOpen ? '✕' : <IconSearch />}
              </button>
              <button
                type="button"
                className="ga-icon-btn"
                title="Избранное"
                aria-label="Избранное"
                onClick={onUtilityFavorites}
              >
                <IconHeart />
              </button>
              <button
                type="button"
                className="ga-icon-btn"
                title={auth ? 'Аккаунт' : 'Войти'}
                aria-label={auth ? 'Аккаунт' : 'Войти'}
                onClick={onUtilityProfile}
              >
                <IconUser />
              </button>
              <button type="button" className="ga-icon-btn ga-icon-bag" title="Корзина" aria-label="Корзина" onClick={onUtilityCart}>
                <IconBag />
                {cartCount > 0 && <span className="ga-bag-badge">{cartCount > 99 ? '99+' : cartCount}</span>}
              </button>
            </div>
            {auth ? (
              <div className="ga-user-inline">
                <span className="ga-user-name">{auth.name}</span>
                <button type="button" className="ga-link-btn" onClick={logout}>
                  выйти
                </button>
              </div>
            ) : (
              <div className="ga-user-inline">
                <button type="button" className="ga-link-btn" onClick={() => setAuthModal('login')}>
                  войти
                </button>
                <button type="button" className="ga-link-btn" onClick={() => setAuthModal('register')}>
                  регистрация
                </button>
              </div>
            )}
          </div>
        </div>

        {headerSearchOpen && (
          <section className="ga-search-panel">
            <form className="ga-search-form" onSubmit={onSearchProducts}>
              <input
                ref={searchInputRef}
                className="ga-search-field"
                placeholder="ХОЧУ КУПИТЬ..."
                title="Один запрос по всем полям. Можно regexp (например уход|крем); если шаблон неверный — обычный поиск."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                autoComplete="off"
              />
              <button type="submit" className="ga-search-submit">
                →
              </button>
            </form>
            <div className="ga-search-showcase">
              <div className="ga-search-columns">
                <div className="ga-search-col">
                  {SEARCH_SUGGESTIONS.map((item) => (
                    <button key={item} type="button" className="ga-search-link" onClick={() => applySearchPreset(item)}>
                      {item}
                    </button>
                  ))}
                </div>
                <div className="ga-search-col">
                  {allKnownBrands.map((item) => (
                    <button key={item} type="button" className="ga-search-link strong" onClick={() => applySearchPreset(item)}>
                      {item}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </section>
        )}

        <nav className="ga-main-nav" aria-label="Разделы магазина">
          <button
            type="button"
            className={`ga-nav-link ${activeNav === 'catalog' ? 'active' : ''}`}
            onClick={goCatalogHome}
          >
            каталог
          </button>
          <button type="button" className={`ga-nav-link ${activeNav === 'brands' ? 'active' : ''}`} onClick={goBrands}>
            бренды
          </button>
          <button type="button" className={`ga-nav-link ${activeNav === 'gift' ? 'active' : ''}`} onClick={goGiftCards}>
            подарочные карты
          </button>
          <button
            type="button"
            className={`ga-nav-link ${activeNav === 'discount' ? 'active' : ''}`}
            onClick={() => goCatalogHome('discount')}
          >
            успей купить
          </button>
          {auth && (
            <button
              type="button"
              className={`ga-nav-link ${activeNav === 'favorites' ? 'active' : ''}`}
              onClick={() => {
                setActiveNav('favorites')
                setSelectedProduct(null)
                setTab('favorites')
              }}
            >
              избранное
            </button>
          )}
          {auth && !isAdmin && (
            <button
              type="button"
              className={`ga-nav-link ${activeNav === 'orders' ? 'active' : ''}`}
              onClick={() => {
                setActiveNav('orders')
                setTab('orders')
              }}
            >
              мои заказы
            </button>
          )}
          {isAdmin && (
            <>
              <button
                type="button"
                className={`ga-nav-link ${activeNav === 'adminUsers' ? 'active' : ''}`}
                onClick={() => {
                  setActiveNav('adminUsers')
                  setTab('adminUsers')
                  loadAdminUsers().catch(() => {})
                  loadAllOrders().catch(() => {})
                }}
              >
                пользователи
              </button>
              <button
                type="button"
                className={`ga-nav-link ${activeNav === 'adminOrders' ? 'active' : ''}`}
                onClick={() => {
                  setActiveNav('adminOrders')
                  setTab('adminOrders')
                  loadAllOrders().catch(() => {})
                }}
              >
                заказы
              </button>
              <button
                type="button"
                className={`ga-nav-link ${activeNav === 'adminBrands' ? 'active' : ''}`}
                onClick={() => {
                  setActiveNav('adminBrands')
                  setTab('adminBrands')
                }}
              >
                добавить бренд
              </button>
              <button
                type="button"
                className={`ga-nav-link ${activeNav === 'adminProducts' ? 'active' : ''}`}
                onClick={() => {
                  setActiveNav('adminProducts')
                  setTab('adminProducts')
                }}
              >
                добавить товар
              </button>
            </>
          )}
        </nav>
      </header>

      {!auth && (
        <div ref={promoStripRef} className="promo-strip">
          Войдите или зарегистрируйтесь, чтобы получить скидку по карте клиента
        </div>
      )}

      {(errorToastText || cartNotice) && (
        <div className="toast-stack" aria-live="polite" aria-atomic="true">
          {errorToastText && <div className="app-toast app-toast--error">{errorToastText}</div>}
          {cartNotice && <div className="app-toast app-toast--success">{cartNotice}</div>}
        </div>
      )}

      {tab === 'catalog' && !selectedOverview &&
        (activeNav === 'gift' ? (
          <section ref={giftHeroRef} className="gift-page">
            <div className="gift-page-banner">
              <div className="gift-page-shell">
                <div className="gift-page-banner-head">
                  <h2 className="gift-page-title">Подарочные карты</h2>
                  <p className="gift-page-subtitle">Отличная идея для подарка близким: онлайн и в магазине</p>
                  <button type="button" className="gift-page-banner-cta" onClick={openGiftWizard}>
                    ВЫБРАТЬ КАРТУ
                  </button>
                </div>
              </div>
            </div>

            {!auth ? (
              <div className="gift-page-main">
                <div className="gift-page-shell">
                <p className="gift-page-lead">
                  Войдите, чтобы увидеть свои карты и баланс. Подарить карту можно после входа.
                </p>
                <button type="button" className="hero-cta gift-login-btn" onClick={() => setAuthModal('login')}>
                  Войти
                </button>
                </div>
              </div>
            ) : (
              <div className="gift-page-main">
                <div className="gift-page-shell">
                <p className="gift-page-total-line">
                  <span className="gift-page-total-label">Всего</span>
                  <span className="gift-page-total-amount">
                    {Number(userWithOrders?.giftBalance ?? 0).toLocaleString('ru-RU', {
                      minimumFractionDigits: 0,
                      maximumFractionDigits: 2,
                    })}{' '}
                    BYN
                  </span>
                </p>

                {giftCardsWithRemaining.length === 0 ? (
                  <p className="muted gift-page-empty">
                    У вас пока нет полученных подарочных карт. Попросите близких отправить подарок на номер вашего
                    аккаунта.
                  </p>
                ) : (
                  <ul className="my-gifts-grid" aria-label="Ваши подарочные карты">
                    {giftCardsWithRemaining.map((g) => (
                      <li key={g.id} className="my-gift-card">
                        <div className="my-gift-card-visual">
                          <img src={giftImageUrlForDesignId(g.designId)} alt="" />
                        </div>
                        <div className="my-gift-card-info">
                          <span className="my-gift-card-date">Подарено {formatGiftReceivedDate(g.createdAt)}</span>
                          <span className="my-gift-card-balance">
                            Остаток:{' '}
                            {Number(g.remaining).toLocaleString('ru-RU', {
                              minimumFractionDigits: 0,
                              maximumFractionDigits: 2,
                            })}{' '}
                            BYN
                          </span>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
                </div>
              </div>
            )}
          </section>
        ) : (
          <section ref={giftHeroRef} className="hero-banner">
            <div className="hero-content">
              <h2>Подарочные карты</h2>
              <p>Отличная идея для подарка близким: онлайн и в магазине</p>
              <button type="button" className="hero-cta" onClick={openGiftWizard}>
                Выбрать карту
              </button>
            </div>
          </section>
        ))}

      {loading && <div className="loading-box">Загрузка...</div>}

      <GiftCardWizard
        open={giftWizardOpen}
        onClose={() => setGiftWizardOpen(false)}
        onError={setError}
        onSuccess={() => setError('Подарочная карта оформлена — получатель увидит начисление по номеру.')}
      />

      <GiftRevealOverlay gift={giftRevealQueue[0] ?? null} onDismiss={dismissGiftReveal} />

      {authModal && (
        <div className="modal-backdrop" role="presentation" onClick={() => setAuthModal(null)}>
          <div
            className="modal panel"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            {authModal === 'login' ? (
              <form onSubmit={onLogin} className="form-grid">
                <h2>Вход</h2>
                {authError && <div className="auth-error">{authError}</div>}
                <input
                  placeholder="+375… или 8 029… или логин"
                  title="Тот же номер, что при регистрации: можно +375 29… или национальный 8 029…"
                  value={loginForm.login}
                  onChange={(e) => setLoginForm((p) => ({ ...p, login: clampLoginInput(e.target.value) }))}
                  autoComplete="username"
                  maxLength={32}
                  required
                />
                <div className="password-row">
                  <input
                    placeholder="Пароль"
                    type={showLoginPassword ? 'text' : 'password'}
                    value={loginForm.password}
                    onChange={(e) => setLoginForm((p) => ({ ...p, password: e.target.value }))}
                    required
                  />
                  <button
                    type="button"
                    className="ghost pass-toggle"
                    onClick={() => setShowLoginPassword((v) => !v)}
                    aria-label={showLoginPassword ? 'Скрыть пароль' : 'Показать пароль'}
                    title={showLoginPassword ? 'Скрыть пароль' : 'Показать пароль'}
                  >
                    {showLoginPassword ? '🙈' : '👁️'}
                  </button>
                </div>
                <button type="submit" className="primary">
                  Войти
                </button>
                <button
                  type="button"
                  className="ghost"
                  onClick={() => {
                    setAuthError('')
                    setAuthModal('register')
                  }}
                >
                  Создать аккаунт
                </button>
              </form>
            ) : (
              <form onSubmit={onRegister} className="form-grid">
                <h2>Регистрация</h2>
                {authError && <div className="auth-error">{authError}</div>}
                <input
                  placeholder="Телефон (+375 29 123 45 67)"
                  type="tel"
                  autoComplete="tel"
                  value={registerForm.phone}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, phone: e.target.value }))}
                  required
                />
                <div className="password-row">
                  <input
                    placeholder="Пароль (от 8 символов)"
                    type={showRegisterPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    value={registerForm.password}
                    onChange={(e) => setRegisterForm((p) => ({ ...p, password: e.target.value }))}
                    required
                    minLength={8}
                  />
                  <button
                    type="button"
                    className="ghost pass-toggle"
                    onClick={() => setShowRegisterPassword((v) => !v)}
                    aria-label={showRegisterPassword ? 'Скрыть пароль' : 'Показать пароль'}
                    title={showRegisterPassword ? 'Скрыть пароль' : 'Показать пароль'}
                  >
                    {showRegisterPassword ? '🙈' : '👁️'}
                  </button>
                </div>
                <label className="field-label">
                  Фамилия
                  <input
                    autoComplete="family-name"
                    value={registerForm.surname}
                    onChange={(e) => setRegisterForm((p) => ({ ...p, surname: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                  />
                </label>
                <label className="field-label">
                  Имя
                  <input
                    autoComplete="given-name"
                    value={registerForm.name}
                    onChange={(e) => setRegisterForm((p) => ({ ...p, name: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                  />
                </label>
                <label className="field-label">
                  Отчество
                  <input
                    autoComplete="additional-name"
                    value={registerForm.patronymic}
                    onChange={(e) => setRegisterForm((p) => ({ ...p, patronymic: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                  />
                </label>
                <button type="submit" className="primary">
                  Зарегистрироваться
                </button>
                <button
                  type="button"
                  className="ghost"
                  onClick={() => {
                    setAuthError('')
                    setAuthModal('login')
                  }}
                >
                  Уже есть аккаунт
                </button>
              </form>
            )}
          </div>
        </div>
      )}

      {tab === 'catalog' && !selectedOverview && activeNav !== 'gift' && (
        <section className="catalog-layout section-pad">
          <div className="catalog-main">
            <div className="catalog-head-row">
              <h2 className="section-title">{activeNav === 'discount' ? 'Успей купить' : 'Хиты'}</h2>
              <div className="filters-top panel">
                <h2>Фильтры</h2>
                <div className="main-filters-row">
                  <label className="field-label">
                    Категория
                    <select value={mainCategoryFilter} onChange={(e) => setMainCategoryFilter(e.target.value)}>
                      {mainCatalogCategories.map((category) => (
                        <option key={category} value={category}>
                          {category === 'all' ? 'Все категории' : category}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="main-filter-checkbox">
                    <input
                      type="checkbox"
                      checked={mainOnlyInStock}
                      onChange={(e) => setMainOnlyInStock(e.target.checked)}
                    />
                    Только в наличии
                  </label>
                </div>
              </div>
            </div>
            {activeNav === 'discount' && productsCatalogList.length === 0 ? (
              <p className="muted">Сейчас нет товаров с остатком 5 шт. и меньше.</p>
            ) : (
            <div className="product-grid">
              {productsCatalogPage.map((product) => (
                (() => {
                  const discountPercent = getDiscountPercent(product)
                  const coverSrc = productImageSrc(product)
                  const stock = getProductStock(product)
                  const outOfStock = stock != null && stock <= 0
                  const inCartQty = Number(cart[product.id] ?? 0)
                  return (
                <article
                  key={product.id}
                  className="product-tile clickable-tile"
                  role="button"
                  tabIndex={0}
                  onClick={() => openProductOverview(product)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault()
                      openProductOverview(product)
                    }
                  }}
                >
                  <div className={`product-visual${coverSrc ? ' product-visual--photo' : ''}`} aria-hidden>
                    {coverSrc && <img className="product-visual-img" src={coverSrc} alt="" decoding="async" />}
                    <button
                      type="button"
                      className="quick-cart-btn"
                      title={outOfStock ? 'Нет в наличии' : 'Быстро добавить в корзину'}
                      disabled={outOfStock}
                      onClick={(e) => {
                        e.stopPropagation()
                        if (outOfStock) return
                        addToCart(product.id, 1)
                      }}
                    >
                      <IconBag />
                      {inCartQty > 0 && <span className="quick-cart-count">{inCartQty}</span>}
                    </button>
                    <button
                      type="button"
                      className={`icon-btn ${favorites.some((item) => String(item.id) === String(product.id)) ? 'active' : ''}`}
                      title={auth ? 'В избранное' : 'Войдите, чтобы добавить в избранное'}
                      onClick={(e) => {
                        e.stopPropagation()
                        onToggleFavorite(product.id)
                      }}
                    >
                      {favorites.some((item) => String(item.id) === String(product.id)) ? '❤' : '♡'}
                    </button>
                    {activeNav === 'discount' && isLastUnit(product) && <LastItemPlaque />}
                    {activeNav === 'discount' && <RunningOutStockBadge stock={stock} />}
                    {discountPercent > 0 && null}
                  </div>
                  <div className="product-body">
                    <div className="product-brand">{product.category ?? 'категория'}</div>
                    <h3>{product.name}</h3>
                    <div className="price-row">
                      <span className="price">{product.salePrice} BYN</span>
                      {!product.inStock && <span className="badge">нет</span>}
                    </div>
                    {isAdmin && (
                      <p className="muted small admin-stock-line">
                        Остаток: {getProductStock(product) ?? 0} шт.
                      </p>
                    )}
                    <div className="tile-actions">
                      <div className="qty-control">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation()
                            addToCart(product.id, -1)
                          }}
                        >
                          −
                        </button>
                        <span>{cart[product.id] ?? 0}</span>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation()
                            addToCart(product.id, 1)
                          }}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>
                </article>
                  )
                })()
              ))}
            </div>
            )}
            <div className="catalog-pagination" aria-label="Пагинация каталога">
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setCatalogPage((p) => Math.max(1, p - 1))}
                disabled={catalogPage <= 1}
              >
                ←
              </button>
              <span className="muted small">
                {catalogPage} / {catalogTotalPages}
              </span>
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setCatalogPage((p) => Math.min(catalogTotalPages, p + 1))}
                disabled={catalogPage >= catalogTotalPages}
              >
                →
              </button>
            </div>

          </div>
        </section>
      )}

      {tab === 'brands' && (
        <section className="brands-page section-pad">
          <div className="brands-layout">
            <aside className="brands-favorites">
              <h2>избранные</h2>
              {favoriteBrands.length === 0 ? (
                <p className="muted small">Пока здесь ничего нет. Добавляйте бренды в свое избранное.</p>
              ) : (
                <div className="brands-favorites-list">
                  {favoriteBrands
                    .slice()
                    .sort((a, b) => a.localeCompare(b, 'ru'))
                    .map((brand) => (
                      <button key={brand} type="button" className="brands-favorite-link" onClick={() => applySearchPreset(brand)}>
                        {brand}
                      </button>
                    ))}
                </div>
              )}
            </aside>
            <div className="brands-main">
              <form className="brands-search-box" onSubmit={onBrandsSearch}>
                <input
                  value={brandQuery}
                  onChange={(e) => setBrandQuery(e.target.value)}
                  placeholder="найти бренды"
                  className="brands-search-input"
                />
                <button type="submit" className="brands-search-icon" aria-label="Искать бренд">
                  ⌕
                </button>
              </form>
              <div className="brands-sections">
                {brandSectionsFiltered.map((section) => (
                  <section
                    key={section.title}
                    id={`brand-section-${encodeURIComponent(section.title)}`}
                    className="brands-section"
                  >
                    <h3>{section.title}</h3>
                    <div className="brands-grid">
                      {section.items.map((item) => (
                        <div key={item} className="brands-link-row">
                          <button type="button" className="brands-link" onClick={() => applySearchPreset(item)}>
                            {item}
                          </button>
                          <button
                            type="button"
                            className={`brands-favorite-btn ${favoriteBrandSet.has(item.toLowerCase()) ? 'active' : ''}`}
                            aria-label={favoriteBrandSet.has(item.toLowerCase()) ? 'Убрать бренд из избранного' : 'Добавить бренд в избранное'}
                            onClick={() => onToggleFavoriteBrand(item)}
                          >
                            {favoriteBrandSet.has(item.toLowerCase()) ? '❤' : '♡'}
                          </button>
                        </div>
                      ))}
                    </div>
                  </section>
                ))}
              </div>
            </div>
            <aside className="brands-index-right" aria-label="Алфавит брендов">
              {brandsIndex.map((ch) => (
                <button key={ch} type="button" className="brands-index-item" onClick={() => jumpToBrandLetter(ch)}>
                  {ch}
                </button>
              ))}
            </aside>
          </div>
        </section>
      )}

      {tab === 'favorites' && auth && (
        <section className="catalog-layout section-pad">
          <div className="catalog-main">
            <h2 className="section-title">Избранное</h2>
            {favorites.length === 0 ? (
              <div className="panel">
                <p className="muted">Пока пусто. Нажмите сердечко на карточке товара, чтобы добавить сюда.</p>
              </div>
            ) : (
              <div className="product-grid">
                {favorites.map((product) => (
                  <FavoriteProductTile
                    key={product.id}
                    product={product}
                    catalogProducts={products}
                    isFavorite
                    onOpenOverview={openProductOverview}
                    onToggleFavorite={onToggleFavorite}
                    onAddToCart={addToCart}
                  />
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {tab === 'account' && auth && (
        <section className="account-page section-pad">
          <div className="account-top">
            <div className="account-profile-card panel">
              <div className="account-avatar-wrap">
                <input
                  ref={avatarInputRef}
                  type="file"
                  className="account-avatar-input"
                  accept="image/jpeg,image/png,image/webp,image/gif"
                  onChange={onAvatarFileChange}
                  tabIndex={-1}
                  aria-hidden
                />
                <button
                  type="button"
                  className={`account-avatar-btn ${avatarPreview ? 'has-photo' : ''}`}
                  onClick={() => avatarInputRef.current?.click()}
                  aria-label="Загрузить или сменить фотографию профиля"
                  title="Сменить фото"
                >
                  <span className="account-avatar-disk">
                    {avatarPreview ? (
                      <img src={avatarPreview} alt="" className="account-avatar-img" decoding="async" />
                    ) : (
                      <span className="account-avatar-initials">{initialsFromSession(auth)}</span>
                    )}
                  </span>
                  <span className="account-avatar-badge" aria-hidden>📷</span>
                </button>
                {avatarPreview ? (
                  <button type="button" className="account-avatar-remove" onClick={clearAvatarPhoto}>
                    Убрать фото
                  </button>
                ) : null}
              </div>
              <p className="account-name">
                {displayPersonalFullName(userWithOrders ?? auth) || auth.login}
              </p>
              <p className="muted small account-login">{auth.login}</p>
              {userWithOrders != null && Number(userWithOrders.giftBalance) > 0 && (
                <p className="account-gift-balance muted small">
                  Подарочный баланс: {Number(userWithOrders.giftBalance).toFixed(2)} BYN
                </p>
              )}
              <form className="account-profile-form" onSubmit={onSaveProfile}>
                <label className="field-label">
                  Фамилия
                  <input
                    value={profileDraft.surname}
                    onChange={(e) => setProfileDraft((d) => ({ ...d, surname: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                    autoComplete="family-name"
                  />
                </label>
                <label className="field-label">
                  Имя
                  <input
                    value={profileDraft.name}
                    onChange={(e) => setProfileDraft((d) => ({ ...d, name: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                    autoComplete="given-name"
                  />
                </label>
                <label className="field-label">
                  Отчество
                  <input
                    value={profileDraft.patronymic}
                    onChange={(e) => setProfileDraft((d) => ({ ...d, patronymic: e.target.value }))}
                    required
                    minLength={2}
                    maxLength={30}
                    autoComplete="additional-name"
                  />
                </label>
                <button type="submit" className="primary account-profile-save" disabled={profileSaving}>
                  {profileSaving ? 'Сохранение…' : 'Сохранить ФИО'}
                </button>
              </form>
              <div className="account-profile-actions">
                <button
                  type="button"
                  className="ghost"
                  onClick={() => {
                    setActiveNav('orders')
                    setTab('orders')
                  }}
                >
                  История заказов
                </button>
              </div>
            </div>
            <div className="account-favorites-block panel">
              <h2 className="account-subtitle">Избранное</h2>
              {favorites.length === 0 ? (
                <p className="muted small">Пока пусто — нажмите сердечко на товаре в каталоге.</p>
              ) : (
                <div className="account-favorites-scroll">
                  <div className="account-favorites-strip">
                    {favorites.map((product) => (
                      <FavoriteProductTile
                        key={product.id}
                        product={product}
                        catalogProducts={products}
                        isFavorite
                        onOpenOverview={openProductOverview}
                        onToggleFavorite={onToggleFavorite}
                        onAddToCart={addToCart}
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
          <div className="account-purchased panel">
            <h2 className="account-subtitle">Товары, которые вы покупали</h2>
            {purchasedProducts.length === 0 ? (
              <p className="muted small">После оформления заказов здесь появятся купленные позиции.</p>
            ) : (
              <div className="product-grid account-purchased-grid">
                {purchasedProducts.map((product) => (
                  <article
                    key={product.id}
                    className="product-tile clickable-tile"
                    role="button"
                    tabIndex={0}
                    onClick={() => openProductOverview(mergeProductWithCatalog(product, products))}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        openProductOverview(mergeProductWithCatalog(product, products))
                      }
                    }}
                  >
                    {(() => {
                      const p = mergeProductWithCatalog(product, products)
                      const coverSrc = productImageSrc(p)
                      const purchasedPrice = resolvePurchasedProductPrice(product, products)
                      const inCartQty = Number(cart[product.id] ?? 0)
                      return (
                    <div className={`product-visual${coverSrc ? ' product-visual--photo' : ''}`} aria-hidden>
                      {coverSrc && <img className="product-visual-img" src={coverSrc} alt="" decoding="async" />}
                      <button
                        type="button"
                        className="quick-cart-btn"
                        title="Быстро добавить в корзину"
                        onClick={(e) => {
                          e.stopPropagation()
                          addToCart(product.id, 1)
                        }}
                      >
                        <IconBag />
                        {inCartQty > 0 && <span className="quick-cart-count">{inCartQty}</span>}
                      </button>
                    </div>
                      )
                    })()}
                    <div className="product-body">
                      <div className="product-brand">{product.category ?? 'категория'}</div>
                      <h3>{product.name}</h3>
                      <p className="muted small purchase-meta">{product._purchaseMeta}</p>
                      <div className="price-row">
                        <span className="price">
                          {(() => {
                            const purchasedPrice = resolvePurchasedProductPrice(product, products)
                            return Number.isFinite(purchasedPrice) ? `${purchasedPrice.toFixed(2)} BYN` : '—'
                          })()}
                        </span>
                      </div>
                      {isAdmin && (
                        <p className="muted small admin-stock-line">
                          Остаток: {getProductStock(product) ?? 0} шт.
                        </p>
                      )}
                      <div className="tile-actions">
                        <button
                          type="button"
                          className={`icon-btn ${favorites.some((x) => String(x.id) === String(product.id)) ? 'active' : ''}`}
                          title="В избранное"
                          onClick={(e) => {
                            e.stopPropagation()
                            onToggleFavorite(product.id)
                          }}
                        >
                          {favorites.some((x) => String(x.id) === String(product.id)) ? '❤' : '♡'}
                        </button>
                        <button
                          type="button"
                          className="ghost"
                          onClick={(e) => {
                            e.stopPropagation()
                            addToCart(product.id, 1)
                          }}
                        >
                          В корзину
                        </button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {selectedOverview && tab === 'catalog' && (
        <section className="product-detail-page">
          <div className="detail-breadcrumb section-pad">
            <button type="button" className="ghost detail-back-btn" onClick={closeProductOverview}>
              ← назад в каталог
            </button>
            <span className="muted small">
              главная / {String(selectedProduct.category ?? 'каталог').toLowerCase()} / товар
            </span>
          </div>

          <div className="detail-hero section-pad">
            <div className="detail-gallery">
              {isLastUnit(selectedProduct) ? <LastItemPlaque variant="hero" /> : null}
              {(() => {
                const src = productImageSrc(selectedProduct)
                return (
                  <div className={`detail-image-main${src ? ' detail-image-main--photo' : ''}`} aria-hidden>
                    {src && <img src={src} alt="" decoding="async" />}
                  </div>
                )
              })()}
            </div>
            <div className="detail-info panel">
              <p className="muted small">{selectedOverview.subtitle}</p>
              <h1>{selectedOverview.title}</h1>
              <div className="detail-volume">{selectedOverview.volumeLabel ?? formatProductVolumeMl(selectedProduct) ?? '—'}</div>
              <div className="detail-price-row">
                <strong>{selectedProduct.salePrice} BYN</strong>
                <span className="muted small">по карте клиента</span>
              </div>
              {isAdmin && (
                <p className="muted small admin-stock-line">
                  Остаток: {getProductStock(selectedProduct) ?? 0} шт.
                </p>
              )}
              <div className="detail-cta-row">
                <button type="button" className="primary" onClick={() => addToCart(selectedOverview.id, 1)}>
                  Добавить в корзину
                </button>
                <button type="button" className="ghost" onClick={() => onToggleFavorite(selectedOverview.id)}>
                  <span className={favorites.some((item) => String(item.id) === String(selectedOverview.id)) ? 'favorite-heart active' : 'favorite-heart'}>
                    {favorites.some((item) => String(item.id) === String(selectedOverview.id)) ? '❤' : '♡'}
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div className="detail-description section-pad panel">
            <h2>Описание</h2>
            <p>{selectedOverview.description}</p>
            <p>{selectedOverview.usage}</p>
            <div className="overview-section">
              <h3>Подробные характеристики</h3>
              <ul className="detail-specs">
                {selectedOverview.specs.map((item) => (
                  <li key={item.label}>
                    <span className="detail-spec-key">{item.label}</span>
                    <span className="detail-spec-dots" aria-hidden />
                    <span className="detail-spec-value">{item.value}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          <div className="detail-rating section-pad panel">
            <h2>Рейтинг и отзывы</h2>
            <div className="detail-rating-grid">
              <div>
                <strong className="detail-big-num">4.8</strong>
                <p className="muted small">оценка товара</p>
              </div>
              <div>
                <strong className="detail-big-num">91%</strong>
                <p className="muted small">покупателей рекомендуют</p>
              </div>
              <div>
                <strong className="detail-big-num">89</strong>
                <p className="muted small">отзывов</p>
              </div>
            </div>
          </div>

          {similarProducts.length > 0 && (
            <div className="detail-similar section-pad">
              <h2>Похожие товары</h2>
              <div className="product-grid">
                {similarProducts.map((product) => (
                (() => {
                  const discountPercent = getDiscountPercent(product)
                  const coverSrc = productImageSrc(product)
                  const inCartQty = Number(cart[product.id] ?? 0)
                  return (
                <article
                  key={product.id}
                  className="product-tile clickable-tile"
                  role="button"
                  tabIndex={0}
                  onClick={() => openProductOverview(product)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault()
                      openProductOverview(product)
                    }
                  }}
                >
                  <div className={`product-visual${coverSrc ? ' product-visual--photo' : ''}`} aria-hidden>
                    {coverSrc && <img className="product-visual-img" src={coverSrc} alt="" decoding="async" />}
                    <button
                      type="button"
                      className="quick-cart-btn"
                      title="Быстро добавить в корзину"
                      onClick={(e) => {
                        e.stopPropagation()
                        addToCart(product.id, 1)
                      }}
                    >
                      <IconBag />
                      {inCartQty > 0 && <span className="quick-cart-count">{inCartQty}</span>}
                    </button>
                    {discountPercent > 0 && null}
                  </div>
                  <div className="product-body">
                    <div className="product-brand">{product.category ?? 'категория'}</div>
                    <h3>{product.name}</h3>
                    <p className="price">от {product.salePrice} BYN</p>
                  </div>
                </article>
                  )
                })()
                ))}
              </div>
            </div>
          )}

          <footer className="detail-footer">
            <div>
              <h3>SilverPear</h3>
              <p className="muted small">скачать приложение • о нас • клиентам • контакты</p>
            </div>
          </footer>
        </section>
      )}

      {tab === 'cart' && (
        <div className="cart-page section-pad" ref={cartBarRef}>
          <section className="cart-layout">
            <header className="cart-header-row">
              <h1>
                корзина <span>/ {cartCount} шт.</span>
              </h1>
            </header>

            <div className="cart-delivery panel">
              <div className="cart-delivery-top">
                <strong>{cartTotal >= FREE_DELIVERY_THRESHOLD ? 'бесплатно из любых ПВЗ' : 'платная доставка'}</strong>
                <span className="muted small">
                  {cartTotal >= FREE_DELIVERY_THRESHOLD
                    ? 'доставка 0 BYN'
                    : `доставка ${deliveryFee.toFixed(2)} BYN · до бесплатной доставки: ${(FREE_DELIVERY_THRESHOLD - cartTotal).toFixed(2)} BYN`}
                </span>
              </div>
              <div className="delivery-track" role="presentation">
                <div
                  className="delivery-progress"
                  style={{ width: `${Math.min(100, (cartTotal / FREE_DELIVERY_THRESHOLD) * 100)}%` }}
                />
              </div>
              <div className="delivery-scale">
                <span>0,00 BYN</span>
                <span>{FREE_DELIVERY_THRESHOLD.toFixed(2).replace('.', ',')} BYN</span>
              </div>
            </div>

            <div className="cart-items panel">
              {Object.keys(cart).length === 0 ? (
                <p className="muted">Добавьте товары из каталога для совершения заказа.</p>
              ) : (
                <ul className="cart-lines cart-lines-rich">
                  {Object.entries(cart).map(([id, qty]) => {
                    const p = products.find((x) => String(x.id) === String(id))
                    const cartImg = p ? productImageSrc(p) : null
                    return (
                      <li key={id} className="cart-line-compact">
                        <div className={`cart-line-thumb${cartImg ? ' cart-line-thumb--photo' : ''}`} aria-hidden>
                          {cartImg && <img src={cartImg} alt="" decoding="async" />}
                        </div>
                        <div className="cart-line-text">
                          <span className="muted small">{p?.category ?? 'товар'}</span>
                          <strong>{p ? p.name : id}</strong>
                          <span className="muted small">{p?.brand ?? 'SilverPear'}</span>
                          <div className="qty-control cart-qty-inline">
                            <button
                              type="button"
                              onClick={() => addToCart(id, -1)}
                              aria-label="Уменьшить количество"
                            >
                              −
                            </button>
                            <span>{qty}</span>
                            <button
                              type="button"
                              onClick={() => addToCart(id, 1)}
                              aria-label="Увеличить количество"
                            >
                              +
                            </button>
                          </div>
                        </div>
                        <strong className="cart-line-sum">{((p?.salePrice ?? 0) * qty).toFixed(2)} BYN</strong>
                      </li>
                    )
                  })}
                </ul>
              )}
            </div>

            <div className="cart-promo panel">
              <form className="cart-promo-form" onSubmit={onApplyPromoCode}>
                <input
                  className="cart-promo-input"
                  type="text"
                  placeholder="ВВЕДИТЕ ПРОМОКОД"
                  value={promoInput}
                  onChange={(e) => setPromoInput(e.target.value)}
                />
                <button type="submit" className="cart-promo-apply" aria-label="Применить промокод">
                  →
                </button>
              </form>
              {appliedPromo?.discountPercent > 0 && (
                <p className="muted small">
                  Скидка по промокоду: {appliedPromo.discountPercent}% ({promoDiscountAmount.toFixed(2)} BYN)
                </p>
              )}
              {appliedPromo?.thankYou && <p className="muted small">СПАСИБО!!!</p>}
            </div>

            <div className="cart-summary panel">
              <h2>сумма заказа</h2>
              <div className="cart-summary-row">
                <span>стоимость продуктов</span>
                <strong>{cartTotal.toFixed(2)} BYN</strong>
              </div>
              <div className="cart-summary-row">
                <span>доставка</span>
                <strong>{deliveryFee.toFixed(2)} BYN</strong>
              </div>
              {promoDiscountAmount > 0 && (
                <div className="cart-summary-row">
                  <span>скидка</span>
                  <strong>-{promoDiscountAmount.toFixed(2)} BYN</strong>
                </div>
              )}
              <div className="cart-summary-total">
                <span>итого</span>
                <strong>{payableBeforeGift.toFixed(2)} BYN</strong>
              </div>
              <div className="cart-bar-buttons">
                <button
                  type="button"
                  className="cart-checkout-btn"
                  disabled={Boolean(auth) && cartCount === 0}
                  onClick={() => {
                    if (!auth) {
                      setAuthModal('login')
                      return
                    }
                    goToCheckout()
                  }}
                >
                  {auth ? 'ОФОРМИТЬ ЗАКАЗ' : 'ВОЙТИ ДЛЯ ЗАКАЗА'}
                </button>
              </div>
            </div>
          </section>
        </div>
      )}

      {tab === 'checkout' && auth && (
        <div className="checkout-page section-pad">
          <section className="checkout-layout">
            <header className="cart-header-row">
              <h1>Оформление заказа</h1>
              <button type="button" className="ghost" onClick={() => setTab('cart')}>
                ← Назад в корзину
              </button>
            </header>

            <div className="cart-items panel">
              <h2 className="checkout-subtitle">Состав</h2>
              <ul className="cart-lines cart-lines-rich">
                {Object.entries(cart).map(([id, qty]) => {
                  const p = products.find((x) => String(x.id) === String(id))
                  const cartImg = p ? productImageSrc(p) : null
                  return (
                    <li key={id} className="cart-line-compact">
                      <div className={`cart-line-thumb${cartImg ? ' cart-line-thumb--photo' : ''}`} aria-hidden>
                        {cartImg && <img src={cartImg} alt="" decoding="async" />}
                      </div>
                      <div className="cart-line-text">
                        <span className="muted small">{p?.category ?? 'товар'}</span>
                        <strong>{p ? p.name : id}</strong>
                        <span className="muted small">
                          {p?.brand ?? 'SilverPear'} · {qty} шт.
                        </span>
                      </div>
                      <strong className="cart-line-sum">{((p?.salePrice ?? 0) * qty).toFixed(2)} BYN</strong>
                    </li>
                  )
                })}
              </ul>
            </div>

            <div className="cart-summary panel checkout-gift-panel">
              <h2>Оплата</h2>
              <div className="cart-summary-row">
                <span>Сумма товаров</span>
                <strong>{cartTotal.toFixed(2)} BYN</strong>
              </div>
              <div className="cart-summary-row">
                <span>Доставка</span>
                <strong>{deliveryFee.toFixed(2)} BYN</strong>
              </div>
              {promoDiscountAmount > 0 && (
                <div className="cart-summary-row">
                  <span>Скидка</span>
                  <strong>-{promoDiscountAmount.toFixed(2)} BYN</strong>
                </div>
              )}
              <p className="muted small checkout-balance-hint">
                Подарочный баланс:{' '}
                <strong>{Number(userWithOrders?.giftBalance ?? 0).toFixed(2)} BYN</strong>
                {Number(userWithOrders?.giftBalance ?? 0) <= 0 && ' — пополните, отправив себе подарочную карту на номер аккаунта'}
              </p>
              <label className="field-label checkout-gift-label">
                Списать с подарочного баланса (не больше суммы заказа)
                <input
                  type="number"
                  inputMode="decimal"
                  min="0"
                  max={Math.min(Number(userWithOrders?.giftBalance ?? 0), payableBeforeGift)}
                  step="0.01"
                  value={checkoutGiftInput}
                  onChange={(e) => setCheckoutGiftInput(e.target.value)}
                  placeholder="0"
                  disabled={payableBeforeGift <= 0 || Number(userWithOrders?.giftBalance ?? 0) <= 0}
                />
              </label>
              <p className="muted small">
                Максимум к списанию:{' '}
                {Math.min(Number(userWithOrders?.giftBalance ?? 0), payableBeforeGift).toFixed(2)} BYN
              </p>
              {appliedPromo?.thankYou && <p className="muted small">СПАСИБО!!!</p>}
              <div className="cart-summary-total">
                <span>К оплате</span>
                <strong>
                  {(
                    payableBeforeGift -
                    Math.min(
                      Math.max(
                        0,
                        Number(String(checkoutGiftInput).replace(',', '.')) || 0,
                      ),
                      Math.min(Number(userWithOrders?.giftBalance ?? 0), payableBeforeGift),
                    )
                  ).toFixed(2)}{' '}
                  BYN
                </strong>
              </div>
              <button type="button" className="primary cart-checkout-btn" onClick={onConfirmCheckout}>
                Подтвердить заказ
              </button>
            </div>
          </section>
        </div>
      )}

      {tab === 'orders' && auth && !isAdmin && (
        <section className="layout-grid two-col section-pad">
          <article className="panel">
            <h2>История заказов</h2>
            {userWithOrders ? (
              <>
                <p className="order-history-user">
                  {displayPersonalFullName(userWithOrders)}{' '}
                  <span className="muted">({userWithOrders.login})</span>
                </p>
                {orderHistorySorted.length === 0 ? (
                  <p className="muted">Заказов пока нет — соберите корзину справа и оформите заказ.</p>
                ) : (
                  <div className="orders-list">
                    {orderHistorySorted.map((order) => (
                      <div className="order-card order-history-card" key={order.id}>
                        <div className="card-top">
                          <strong>{order.orderNumber ?? `#${order.id}`}</strong>
                          <span className="order-history-meta">{formatOrderDate(order.orderDate)}</span>
                        </div>
                        <p className="order-status-line">
                          <span className="muted small">статус:</span>{' '}
                          {ORDER_STATUS_LABELS[order.status] ?? order.status}
                        </p>
                        <p className="order-total-line">
                          <strong>{order.totalAmount} BYN</strong>
                          {order.giftCardAppliedAmount != null &&
                            Number(order.giftCardAppliedAmount) > 0 && (
                            <span className="muted small order-gift-meta">
                              {' '}
                              · с баланса {Number(order.giftCardAppliedAmount).toFixed(2)} BYN · к оплате{' '}
                              {(Number(order.totalAmount) - Number(order.giftCardAppliedAmount)).toFixed(2)} BYN
                            </span>
                          )}
                        </p>
                        {Array.isArray(order.orderItems) && order.orderItems.length > 0 ? (
                          <ul className="order-items-list">
                            {order.orderItems.map((item) => (
                              <li key={item.id}>
                                {item.product?.name ?? 'Товар'} × {item.quantity}
                                {item.priceAtTime != null && (
                                  <span className="muted small"> ({item.priceAtTime} BYN/шт.)</span>
                                )}
                              </li>
                            ))}
                          </ul>
                        ) : (
                          <p className="muted small">Состав заказа пока недоступен. Обновите страницу.</p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </>
            ) : (
              <p className="muted">Нет данных профиля.</p>
            )}
          </article>

          <article className="panel orders-cart-panel">
            <h2>Корзина</h2>
            <p className="muted small">
              Добавьте товары из каталога — они появятся здесь. Нажмите «Оформить заказ», чтобы перейти к оплате.
            </p>
            {cartCount === 0 ? (
              <p className="muted">Корзина пуста. Перейдите в каталог и выберите товары.</p>
            ) : (
              <>
                <ul className="cart-lines cart-lines-rich orders-cart-preview">
                  {Object.entries(cart).map(([id, qty]) => {
                    const p = products.find((x) => String(x.id) === String(id))
                    const cartImg = p ? productImageSrc(p) : null
                    return (
                      <li key={id} className="cart-line-compact">
                        <div className={`cart-line-thumb${cartImg ? ' cart-line-thumb--photo' : ''}`} aria-hidden>
                          {cartImg && <img src={cartImg} alt="" decoding="async" />}
                        </div>
                        <div className="cart-line-text">
                          <span className="muted small">{p?.category ?? 'товар'}</span>
                          <strong>{p ? p.name : id}</strong>
                          <span className="muted small">
                            {p?.brand ?? 'SilverPear'} · {qty} шт.
                          </span>
                        </div>
                        <strong className="cart-line-sum">{((p?.salePrice ?? 0) * qty).toFixed(2)} BYN</strong>
                      </li>
                    )
                  })}
                </ul>
                <p className="orders-cart-total">
                  <strong>Итого: {cartTotal.toFixed(2)} BYN</strong>
                </p>
                {userWithOrders != null && Number(userWithOrders.giftBalance) > 0 && (
                  <p className="muted small">
                    Подарочный баланс: {Number(userWithOrders.giftBalance).toFixed(2)} BYN
                  </p>
                )}
                <div className="orders-cart-actions">
                  <button type="button" className="primary" onClick={onUtilityCart}>
                    Оформить заказ
                  </button>
                  <button
                    type="button"
                    className="ghost small-btn"
                    onClick={() => {
                      setTab('catalog')
                      goCatalogHome()
                    }}
                  >
                    В каталог
                  </button>
                </div>
              </>
            )}
          </article>

        </section>
      )}

      {tab === 'adminOrders' && isAdmin && (
        <section className="section-pad">
          <article className="panel">
            <h2>Заказы</h2>
            <div className="admin-orders-filters">
              <label className="field-label admin-orders-filter admin-orders-filter--status">
                Статус
                <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                  <option value="">Все статусы</option>
                  <option value="NEW">Новый</option>
                  <option value="PROCESSED">В работе</option>
                  <option value="DELIVERED">Доставлен</option>
                  <option value="TRANSFERRED_FOR_DELIVERY">Передан</option>
                  <option value="EXPECTS">Ожидает</option>
                  <option value="CANCELLED">Отменен</option>
                </select>
              </label>
              <label className="field-label admin-orders-filter admin-orders-filter--sort">
                Сортировка
                <select value={adminOrderSort} onChange={(e) => setAdminOrderSort(e.target.value)}>
                  <option value="newest">Новые сначала</option>
                  <option value="oldest">Старые сначала</option>
                </select>
              </label>
            </div>
            <div className="orders-list">
              {adminOrdersPageItems.map((order) => (
                <div className="order-card" key={order.id}>
                  <div className="card-top">
                    <strong>{order.orderNumber ?? `#${order.id}`}</strong>
                    <button type="button" onClick={() => onDeleteOrder(order.id)}>
                      Удалить
                    </button>
                  </div>
                  <p className="admin-order-status">{ORDER_STATUS_LABELS[order.status] ?? order.status}</p>
                  <div className="admin-order-status-row">
                    <select
                      value={order.status ?? 'NEW'}
                      onChange={(e) => onAdminUpdateOrderStatus(order, e.target.value)}
                    >
                      <option value="NEW">Новый</option>
                      <option value="PROCESSED">В работе</option>
                      <option value="DELIVERED">Доставлен</option>
                      <option value="TRANSFERRED_FOR_DELIVERY">Передан</option>
                      <option value="EXPECTS">Ожидает</option>
                      <option value="CANCELLED">Отменен</option>
                    </select>
                    <span className="muted small">{formatOrderDate(order.orderDate)}</span>
                  </div>
                  <p>
                    {order.totalAmount} BYN
                    {order.giftCardAppliedAmount != null && Number(order.giftCardAppliedAmount) > 0 && (
                      <span className="muted small"> (баланс {Number(order.giftCardAppliedAmount).toFixed(2)})</span>
                    )}
                  </p>
                  <p className="muted small">Состав заказа:</p>
                  <ul className="admin-order-items">
                    {order.orderItems?.map((item) => (
                      <li key={item.id}>
                        {item.product?.name ?? 'Товар'} × {item.quantity}
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
            <div className="catalog-pagination" aria-label="Пагинация заказов">
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminOrdersPage((p) => Math.max(1, p - 1))}
                disabled={adminOrdersPage <= 1}
              >
                ←
              </button>
              <span className="muted small">
                {adminOrdersPage} / {adminOrdersTotalPages}
              </span>
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminOrdersPage((p) => Math.min(adminOrdersTotalPages, p + 1))}
                disabled={adminOrdersPage >= adminOrdersTotalPages}
              >
                →
              </button>
            </div>
          </article>
        </section>
      )}

      {tab === 'adminUsers' && isAdmin && (
        <section className="layout-grid two-col section-pad admin-users-layout">
          <article className="panel">
            <h2>Поиск пользователей</h2>
            <input
              type="search"
              placeholder="Поиск: id, логин, ФИО, телефон"
              value={adminUsersQuery}
              onChange={(e) => setAdminUsersQuery(e.target.value)}
            />
            <p className="muted small">Найдено: {adminUsersFiltered.length}</p>
          </article>

          <article className="panel">
            <h2>Список пользователей</h2>
            <ul className="simple-list">
              {adminUsersPageItems.map((user) => (
                <li key={user.id}>
                  <span>
                    <strong>
                      #{user.id} {user.login}
                    </strong>
                    <br />
                    <span className="muted small">
                      {[user.surname, user.name, user.patronymic].filter(Boolean).join(' ') || '—'}
                      {user.passwordMasked ? ` · пароль: ${user.passwordMasked}` : ''}
                    </span>
                    <br />
                    {(() => {
                      const userOrders = Array.isArray(user.orders) && user.orders.length
                        ? user.orders
                        : orders.filter((order) => String(order.userId) === String(user.id))
                      if (!userOrders.length) return <span className="muted small">Заказов нет</span>
                      const top = userOrders
                        .slice()
                        .sort((a, b) => new Date(b.orderDate ?? 0).getTime() - new Date(a.orderDate ?? 0).getTime())
                        .slice(0, 3)
                      return (
                        <span className="muted small">
                          {top
                            .map((order) => {
                              const date = formatOrderDate(order.orderDate)
                              const status = ORDER_STATUS_LABELS[order.status] ?? order.status
                              const productsPreview = (order.orderItems ?? [])
                                .map((item) => item?.product?.name)
                                .filter(Boolean)
                                .slice(0, 2)
                                .join(', ')
                              return `${date} · ${status}${productsPreview ? ` · ${productsPreview}` : ''}`
                            })
                            .join(' | ')}
                        </span>
                      )
                    })()}
                  </span>
                  <span className="row-actions">
                    <button
                      type="button"
                      onClick={() => onStartEditUser(user)}
                    >
                      Изменить
                    </button>
                    <button type="button" onClick={() => onDeleteUser(user.id)}>
                      Удалить
                    </button>
                  </span>
                  {editingUserId === user.id && editingUserDraft && (
                    <div className="form-grid admin-user-edit-form">
                      <input
                        placeholder="+375XXXXXXXXX"
                        value={editingUserDraft.login}
                        onChange={(e) => setEditingUserDraft((p) => ({ ...p, login: e.target.value }))}
                      />
                      <div className="admin-password-row">
                        <input
                          placeholder="Пароль"
                          type={showEditUserPassword ? 'text' : 'password'}
                          value={editingUserDraft.password}
                          onChange={(e) => setEditingUserDraft((p) => ({ ...p, password: e.target.value }))}
                        />
                        <button
                          type="button"
                          className="ghost small-btn"
                          onClick={() => setShowEditUserPassword((v) => !v)}
                        >
                          {showEditUserPassword ? 'Скрыть' : 'Показать'}
                        </button>
                      </div>
                      <input
                        placeholder="Имя"
                        value={editingUserDraft.name}
                        onChange={(e) => setEditingUserDraft((p) => ({ ...p, name: e.target.value }))}
                      />
                      <input
                        placeholder="Фамилия"
                        value={editingUserDraft.surname}
                        onChange={(e) => setEditingUserDraft((p) => ({ ...p, surname: e.target.value }))}
                      />
                      <input
                        placeholder="Отчество"
                        value={editingUserDraft.patronymic}
                        onChange={(e) => setEditingUserDraft((p) => ({ ...p, patronymic: e.target.value }))}
                      />
                      <div className="row-actions">
                        <button type="button" className="primary" onClick={onSaveEditedUser}>
                          Сохранить
                        </button>
                        <button type="button" className="ghost" onClick={onCancelEditUser}>
                          Отмена
                        </button>
                      </div>
                      <div className="admin-user-orders-full">
                        <p className="muted small"><strong>Заказы пользователя:</strong></p>
                        {(() => {
                          const fullOrders = orders
                            .filter((order) => String(order.userId) === String(user.id))
                            .slice()
                            .sort((a, b) => new Date(b.orderDate ?? 0).getTime() - new Date(a.orderDate ?? 0).getTime())
                          if (!fullOrders.length) return <p className="muted small">Заказов нет</p>
                          return (
                            <ul className="admin-order-items">
                              {fullOrders.map((order) => (
                                <li key={order.id}>
                                  {formatOrderDate(order.orderDate)} · {(order.orderItems ?? [])
                                    .map((item) => `${item?.product?.name ?? 'Товар'} x${item?.quantity ?? 0}`)
                                    .join(', ')}
                                  {` · ${Number(order.totalAmount ?? 0).toFixed(2)} BYN`}
                                </li>
                              ))}
                            </ul>
                          )
                        })()}
                      </div>
                    </div>
                  )}
                </li>
              ))}
            </ul>
            <div className="catalog-pagination" aria-label="Пагинация пользователей">
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminUsersPage((p) => Math.max(1, p - 1))}
                disabled={adminUsersPage <= 1}
              >
                ←
              </button>
              <span className="muted small">
                {adminUsersPage} / {adminUsersTotalPages}
              </span>
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminUsersPage((p) => Math.min(adminUsersTotalPages, p + 1))}
                disabled={adminUsersPage >= adminUsersTotalPages}
              >
                →
              </button>
            </div>
          </article>
        </section>
      )}

      {tab === 'adminBrands' && isAdmin && (
        <section className="layout-grid two-col section-pad">
          <article className="panel">
            <h2>Добавить бренд</h2>
            <form onSubmit={onAddManagedBrand} className="form-grid">
              <input
                placeholder="Название бренда"
                value={adminBrandInput}
                onChange={(e) => setAdminBrandInput(e.target.value)}
              />
              <button type="submit" className="primary">Добавить бренд</button>
            </form>
            <p className="muted small">Бренды появятся в поиске и на странице брендов.</p>
          </article>
          <article className="panel">
            <h2>Список брендов</h2>
            <ul className="simple-list compact">
              {allKnownBrands.map((brand) => (
                <li key={brand}>
                  <span>{brand}</span>
                  <span className="row-actions">
                    <button type="button" onClick={() => onRemoveManagedBrand(brand)}>Удалить</button>
                  </span>
                </li>
              ))}
            </ul>
          </article>
        </section>
      )}

      {tab === 'adminProducts' && isAdmin && (
        <section className="section-pad">
          <article className="panel">
            <h2>Добавить товар</h2>
            <form onSubmit={onSaveProduct} className="form-grid">
              <input
                placeholder="Название"
                value={productForm.name}
                onChange={(e) => setProductForm((p) => ({ ...p, name: e.target.value }))}
                required
              />
              <label className="field-label">
                Бренд
                <select
                  value={productForm.brand}
                  onChange={(e) => setProductForm((p) => ({ ...p, brand: e.target.value }))}
                  required
                >
                  <option value="">Выберите бренд</option>
                  {adminBrandOptions.map((brand) => (
                    <option key={brand} value={brand}>
                      {brand}
                    </option>
                  ))}
                </select>
              </label>
              <input
                placeholder="Категория"
                value={productForm.category}
                onChange={(e) => setProductForm((p) => ({ ...p, category: e.target.value }))}
                required
              />
              <div className="row two">
                <input
                  type="number"
                  step="0.01"
                  placeholder="Цена"
                  value={productForm.salePrice}
                  onChange={(e) => setProductForm((p) => ({ ...p, salePrice: e.target.value }))}
                  required
                />
                <input
                  type="number"
                  placeholder="Объём"
                  value={productForm.volume}
                  onChange={(e) => setProductForm((p) => ({ ...p, volume: e.target.value }))}
                />
              </div>
              <div className="row two">
                <input
                  type="number"
                  placeholder="Остаток"
                  value={productForm.stockQuantity}
                  onChange={(e) => setProductForm((p) => ({ ...p, stockQuantity: e.target.value }))}
                />
                <select
                  value={productForm.gender}
                  onChange={(e) => setProductForm((p) => ({ ...p, gender: e.target.value }))}
                >
                  <option value="UNISEX">UNISEX</option>
                  <option value="FEMALE">FEMALE</option>
                  <option value="MALE">MALE</option>
                </select>
              </div>

              <div className="row two">
                <input
                  placeholder="Тип продукта"
                  value={productForm.productType}
                  onChange={(e) => setProductForm((p) => ({ ...p, productType: e.target.value }))}
                />
                <select
                  value={productForm.skinType ?? ''}
                  onChange={(e) => setProductForm((p) => ({ ...p, skinType: e.target.value }))}
                >
                  <option value="">Тип кожи</option>
                  {SKIN_TYPE_OPTIONS.map((item) => (
                    <option key={item} value={item}>
                      {item}
                    </option>
                  ))}
                </select>
              </div>

              {productExtraSpecs.map((spec, idx) => (
                <div className="admin-spec-row" key={`spec-${idx}`}>
                  <select value={spec.key} onChange={(e) => onUpdateExtraSpec(idx, { key: e.target.value })}>
                    <option value="">Характеристика</option>
                    {[...new Set([...EXTRA_SPEC_OPTIONS, ...productExtraSpecs.map((x) => String(x?.key ?? '').trim()).filter(Boolean)])].map((item) => (
                      <option key={item} value={item}>
                        {item}
                      </option>
                    ))}
                  </select>
                  <input
                    placeholder="Значение"
                    value={spec.value}
                    onChange={(e) => onUpdateExtraSpec(idx, { value: e.target.value })}
                  />
                  <button type="button" className="ghost admin-spec-remove" onClick={() => onRemoveExtraSpec(idx)}>
                    ×
                  </button>
                </div>
              ))}
              <button type="button" className="ghost" onClick={onAddExtraSpec}>
                + Добавить характеристику
              </button>

              <input
                type="file"
                accept="image/*"
                onChange={(e) => onAdminProductImagePick(e.target.files?.[0] ?? null)}
              />
              <textarea
                placeholder="Описание"
                value={productForm.description}
                onChange={(e) => setProductForm((p) => ({ ...p, description: e.target.value }))}
                rows={5}
              />
              <button type="submit" className="primary">
                {productForm.id ? 'Сохранить товар' : 'Создать товар'}
              </button>
              {productForm.id && (
                <button
                  type="button"
                  className="ghost"
                  onClick={() => {
                    setProductForm(initialProductForm)
                    setProductExtraSpecs([{ key: '', value: '' }])
                  }}
                >
                  Новый товар
                </button>
              )}
            </form>
          </article>

          <article className="panel">
            <h2>Товары</h2>
            <input
              type="search"
              placeholder="Поиск: id, название, бренд, категория"
              value={adminProductsQuery}
              onChange={(e) => setAdminProductsQuery(e.target.value)}
            />
            <ul className="simple-list compact">
              {adminProductsPageItems.map((product) => (
                <li key={product.id}>
                  <span>
                    #{product.id} {product.name} ({product.brand})
                    <br />
                    <span className="muted small">
                      Остаток: {getProductStock(product) ?? 0} шт.
                    </span>
                    <br />
                    <span className="muted small">
                      {extractDescriptionSpecs(product.description)
                        .slice(0, 3)
                        .map((spec) => `${spec.key}: ${spec.value}`)
                        .join(' · ') || 'Характеристики не указаны'}
                    </span>
                  </span>
                  <span className="row-actions">
                    <button
                      type="button"
                      onClick={() => {
                        const extractedSpecs = extractDescriptionSpecs(product.description ?? '')
                        setProductForm({
                          id: product.id,
                          name: product.name ?? '',
                          brand: product.brand ?? '',
                          category: product.category ?? '',
                          salePrice: product.salePrice ?? '',
                          oldSalePrice: product.oldSalePrice ?? 0,
                          inStock: product.inStock ?? true,
                          stockQuantity: product.stockQuantity ?? '',
                          productType: extractMetaValue(product.description, 'тип продукта') ?? product.type ?? '',
                          skinType: extractMetaValue(product.description, 'тип кожи') ?? '',
                          gender: product.gender ?? 'UNISEX',
                          volume: product.volume ?? '',
                          imageUrl: product.imageUrl ?? '',
                          description: stripAdminMetaFromDescription(product.description ?? ''),
                        })
                        setProductExtraSpecs(extractedSpecs.length ? extractedSpecs : [{ key: '', value: '' }])
                      }}
                    >
                      Изменить
                    </button>
                    <button type="button" onClick={() => onDeleteProduct(product.id)}>
                      Удалить
                    </button>
                  </span>
                </li>
              ))}
            </ul>
            <div className="catalog-pagination" aria-label="Пагинация товаров">
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminProductsPage((p) => Math.max(1, p - 1))}
                disabled={adminProductsPage <= 1}
              >
                ←
              </button>
              <span className="muted small">
                {adminProductsPage} / {adminProductsTotalPages}
              </span>
              <button
                type="button"
                className="ghost small-btn"
                onClick={() => setAdminProductsPage((p) => Math.min(adminProductsTotalPages, p + 1))}
                disabled={adminProductsPage >= adminProductsTotalPages}
              >
                →
              </button>
            </div>
          </article>
        </section>
      )}
    </div>
  )
}

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function buildProductOverview(product) {
  const id = Number(product.id)
  const nameLower = String(product.name ?? '').toLowerCase()
  const brandLower = String(product.brand ?? '').toLowerCase()
  const apiDescription = normalizeProductDescription(product.description)
  const productTypeMeta = extractMetaValue(product.description, 'тип продукта')
  const skinTypeMeta = extractMetaValue(product.description, 'тип кожи')
  const vol = formatProductVolumeMl(product)

  const isVivienne =
    id === 30 ||
    (nameLower.includes('fixateur') && nameLower.includes('lamination')) ||
    (brandLower.includes('vivienne') && nameLower.includes('fixateur'))

  const isGoTapaBakuchiol =
    id === 31 ||
    (brandLower.includes('go tapa') && nameLower.includes('bakuchiol')) ||
    nameLower.includes('bakuchiol firming cream')

  if (isVivienne) {
    return {
      id: product.id,
      title: 'VIVIENNE SABO Fixateur Lamination',
      subtitle: `${product.brand ?? 'VIVIENNE SABO'} · гель для бровей`,
      description:
        'FIXATEUR LAMINATION EFFECT — гель для бровей от VIVIENNE SABÓ для экстрасильной фиксации с эффектом ламинирования на 16 часов. Благодаря его суперстойкой формуле и удобной щеточке получится уложить даже непослушные и жесткие брови.\n\n' +
        'Гель с ультралегкой текстурой мгновенно придает форму бровям и моментально фиксирует их без склеивания и белого налета. Спиралевидная щеточка среднего размера с ворсинками набирает нужное количество массы, чтобы придать желаемую форму даже жестким бровям, добавить им объем и текстурность.\n\n' +
        'С FIXATEUR LAMINATION EFFECT легко создать естественную укладку и фиксацию с эффектом ламинирования без похода в салон.',
      effects: [
        'экстрасильная фиксация до 16 часов',
        'эффект ламинирования без салона',
        'без склеивания и белого налета',
        'удобная спиралевидная щеточка',
      ],
      ingredients: ['ультралегкая фиксирующая формула'],
      usage:
        'Уложите брови щеточкой в желаемом направлении, при необходимости дайте просохнуть. Снимайте вечером средством для снятия макияжа.',
      specs: [
        { label: 'тип продукта', value: productTypeMeta ?? 'гель для бровей' },
        { label: 'назначение', value: 'фиксация' },
        { label: 'тип кожи', value: skinTypeMeta ?? 'для всех типов кожи' },
        { label: 'область применения', value: 'брови' },
        { label: 'финиш', value: 'глянцевый' },
        { label: 'текстура', value: 'жидкая' },
        { label: 'объём', value: '6 мл' },
      ],
      volumeLabel: '6 мл',
    }
  }

  if (isGoTapaBakuchiol) {
    return {
      id: product.id,
      title: 'GO TAPA Bakuchiol firming cream',
      subtitle: `${product.brand ?? 'GO TAPA'} · крем для лица`,
      description:
        'Для чего: Когда гравитация начинает работать против вас.\n\n' +
        'Легкая, как перышко, текстура крема основательно укрепляет кожу, увлажняет и противостоит потере влаги. Мощный бакучиол, суперзвезда современного антивозрастного ухода, работает на уровне дермы и усиливает синтез коллагена и замедляет его распад. Идеальный вариант, если требуется эффект ретинола, но без раздражения. Крем достаточно легкий, чтобы наносить и на зону вокруг глаз - так чего же вы ждете?\n\n' +
        'Для лучшего проникновения крема и более видимого эффекта, перед нанесением используйте сыворотку. Подходит всем типам кожи.',
      effects: [
        'усиливает синтез коллагена',
        'предотвращает потерю упругости',
        'укрепляет кожный каркас',
      ],
      ingredients: ['бакучиол'],
      usage:
        'Наносите на очищенную кожу лица и вокруг глаз. Для лучшего эффекта используйте после сыворотки.',
      specs: [
        { label: 'тип продукта', value: 'крем для лица' },
        { label: 'для кого', value: 'универсально' },
        { label: 'назначение', value: 'против признаков старения, увлажнение' },
        { label: 'тип кожи', value: 'для всех типов кожи' },
        { label: 'область применения', value: 'лицо' },
        { label: 'время нанесения', value: 'в любое время' },
        { label: 'объём', value: '10 мл' },
      ],
      volumeLabel: '10 мл',
    }
  }

  if (id === 29 || (brandLower.includes('darling') && nameLower.includes('glisten'))) {
    return {
      id: product.id,
      title: 'DARLING Glisten',
      subtitle: `${product.brand ?? 'DARLING'} · хайлайтер-стик`,
      description:
        apiDescription ||
        'Я твой легкий ХАЙЛАЙТЕР-СТИК для свежего образа с эффектом влажного сияния. Шелковая текстура ложится как вторая кожа; насыщен увлажняющим фитоскваланом.',
      effects: ['сияние с эффектом влажной кожи', 'лёгкая растушёвка', 'уход с фитоскваланом'],
      ingredients: ['фитосквалан'],
      usage: 'Нанесите на зоны сияния и растушуйте пальцами или кистью.',
      specs: [
        { label: 'тип продукта', value: 'хайлайтер' },
        { label: 'финиш', value: 'влажное сияние' },
        { label: 'область применения', value: 'лицо' },
      ],
      volumeLabel: vol ?? 'стик',
    }
  }

  if (id === 28 || nameLower.includes('go bright allover')) {
    return {
      id: product.id,
      title: 'RAD Go Bright Allover',
      subtitle: `${product.brand ?? 'RAD'} · сияющий стик`,
      description: apiDescription || 'Формат стика для естественного сияния — быстро и удобно.',
      effects: ['естественное сияние', 'удобный стик', 'лёгкое нанесение'],
      ingredients: [],
      usage: 'Проведите стиком по коже и при необходимости растушуйте.',
      specs: [
        { label: 'тип продукта', value: 'хайлайтер, стик' },
        { label: 'финиш', value: 'сияние' },
        { label: 'область применения', value: 'лицо' },
      ],
      volumeLabel: vol ?? 'стик',
    }
  }

  if (id === 27 || nameLower.includes('shine on thru')) {
    return {
      id: product.id,
      title: 'RAD Shine on Thru Highlighter',
      subtitle: `${product.brand ?? 'RAD'} · хайлайтер`,
      description: apiDescription || 'Пудровая текстура с настраиваемой интенсивностью сияния.',
      effects: ['настраиваемое сияние', 'пудровая текстура', 'от лёгкого glow до яркого блеска'],
      ingredients: [],
      usage: 'Нанесите кистью на выступающие части лица; наслаивайте для большей интенсивности.',
      specs: [
        { label: 'тип продукта', value: 'хайлайтер' },
        { label: 'текстура', value: 'пудровая' },
        { label: 'финиш', value: 'сияние' },
      ],
      volumeLabel: vol ?? '—',
    }
  }

  if (id === 26 || nameLower.includes('planeta organica pure')) {
    return {
      id: product.id,
      title: 'PLANETA ORGANICA PURE',
      subtitle: `${product.brand ?? 'PLANETA ORGANICA'} · шампунь`,
      description:
        apiDescription ||
        'Мягкий шампунь для чувствительной кожи головы: ежедневное использование, деликатное очищение.',
      effects: ['мягкое очищение', 'гипоаллергенная формула', 'блеск и мягкость волос'],
      ingredients: ['ExtPine®', 'индийский мыльный орех', 'огуречная трава'],
      usage: 'Вспеньте на влажных волосах, помассируйте кожу головы, смойте тёплой водой.',
      specs: [
        { label: 'тип продукта', value: 'шампунь' },
        { label: 'назначение', value: 'очищение' },
        { label: 'тип кожи головы', value: 'чувствительная, склонная к аллергии' },
        { label: 'объём', value: '400 мл' },
      ],
      volumeLabel: '400 мл',
    }
  }

  if (id === 25 || (brandLower.includes('essence') && nameLower.includes('princess'))) {
    return {
      id: product.id,
      title: 'ESSENCE Lash PRINCESS',
      subtitle: `${product.brand ?? 'ESSENCE'} · тушь`,
      description:
        apiDescription ||
        'Бордовая тушь с эффектом накладных ресниц и конусообразной фибровой щеточкой.',
      effects: ['длина и объём', 'эффект накладных ресниц', 'ухаживающие растительные воски'],
      ingredients: ['растительные воски'],
      usage: 'Проведите щеточкой от корней к кончикам; при необходимости нанесите второй слой.',
      specs: [
        { label: 'тип продукта', value: 'тушь' },
        { label: 'оттенок', value: 'бордовый' },
        { label: 'эффект', value: 'объём, длина' },
        { label: 'объём', value: '12 мл' },
      ],
      volumeLabel: '12 мл',
    }
  }

  if (id === 24 || nameLower.includes('bright lift day')) {
    return {
      id: product.id,
      title: 'KIKO MILANO Bright Lift Day',
      subtitle: `${product.brand ?? 'KIKO MILANO'} · дневной крем`,
      description:
        apiDescription ||
        'Осветляющий дневной лифтинг-крем с морским коллагеном и SPF; для нормальной и сухой кожи.',
      effects: ['тонус и сияние', 'уход против морщин', 'улучшение стойкости макияжа'],
      ingredients: ['морской коллаген', 'витамин C', 'ActiGlow'],
      usage: 'Наносите утром на очищенное лицо равномерным слоем.',
      specs: [
        { label: 'тип продукта', value: 'дневной крем' },
        { label: 'тип кожи', value: 'нормальная и сухая' },
        { label: 'SPF', value: 'да (в составе)' },
        { label: 'объём', value: '50 мл' },
      ],
      volumeLabel: '50 мл',
    }
  }

  if (id === 23 || (brandLower.includes('neydo') && nameLower.includes('mossland'))) {
    return {
      id: product.id,
      title: 'NEYDO Mossland 12.09',
      subtitle: `${product.brand ?? 'NEYDO'} · парфюм`,
      description:
        apiDescription ||
        'Аромат-приключение с можжевельником и ветивером, цветочные ноты гвоздики и пряные специи.',
      effects: ['древесно-цветочный характер', 'насыщенный характер', 'унисекс-формат'],
      ingredients: ['можжевельник', 'ветивер', 'гвоздика', 'пряности'],
      usage: 'Наносите на пульс-точки: шея, запястья.',
      specs: [
        { label: 'тип продукта', value: 'парфюмерная вода' },
        { label: 'объём', value: '50 мл' },
        { label: 'пол', value: 'унисекс' },
      ],
      volumeLabel: '50 мл',
    }
  }

  const isPdrnLike = nameLower.includes('pdrn') || nameLower.includes('aqua power')

  if (isPdrnLike) {
    return {
      id: product.id,
      title: 'PDRN AQUA POWER',
      subtitle: `${product.brand ?? 'Medi-Peel'} · увлажняющая и восстанавливающая сыворотка`,
      description:
        'Обзор в стиле карточки Gold Apple: интенсивно увлажняющая формула для тусклой и обезвоженной кожи с акцентом на восстановление барьера и визуальную гладкость.',
      effects: [
        'быстрое насыщение влагой без липкости',
        'снижение ощущения стянутости и сухости',
        'более ровный и свежий тон кожи',
        'поддержка восстановления после агрессивного ухода',
      ],
      ingredients: ['PDRN-комплекс', 'гиалуроновая кислота', 'пантенол', 'ниацинамид'],
      usage:
        'Нанесите 1-2 нажатия на очищенную кожу лица после тонера, распределите до впитывания. Утром завершайте SPF-защитой.',
      specs: [
        { label: 'тип продукта', value: 'тонер' },
        { label: 'назначение', value: 'восстановление, увлажнение, питание' },
        { label: 'тип кожи', value: 'для сухой кожи' },
        { label: 'действующий компонент', value: 'pdrn, экзосомы, глюконолактон' },
        { label: 'время нанесения', value: 'в любое время' },
        { label: 'объем', value: '150 мл' },
      ],
      volumeLabel: '150 мл',
    }
  }

  return {
    id: product.id,
    title: product.name,
    subtitle: `${product.brand ?? 'SilverPear'} · ${product.category ?? 'Уход'}`,
    description:
      apiDescription ||
      'Краткий обзор товара: комфортная текстура, ежедневное применение и аккуратный уходовый результат.',
    effects: ['мягкое воздействие на кожу', 'комфорт в течение дня', 'подходит для регулярного применения'],
    ingredients: ['активные увлажняющие компоненты', 'смягчающие агенты', 'поддерживающие экстракты'],
    usage: 'Наносите по инструкции бренда 1-2 раза в день на чистую кожу.',
    specs: [
      { label: 'тип продукта', value: productTypeMeta ?? product.category ?? 'уход' },
      { label: 'назначение', value: 'ежедневный уход и поддержка комфорта кожи' },
      { label: 'тип кожи', value: skinTypeMeta ?? 'универсально' },
      { label: 'время нанесения', value: 'утро/вечер' },
    ],
    volumeLabel: vol ?? undefined,
  }
}

const initialProductForm = {
  id: null,
  name: '',
  brand: '',
  description: '',
  category: '',
  salePrice: '',
  oldSalePrice: 0,
  inStock: true,
  stockQuantity: '',
  productType: '',
  skinType: '',
  gender: 'FEMALE',
  volume: '',
  imageUrl: '',
}

const SEARCH_SUGGESTIONS = [
  'энзимная пудра',
  'тушь',
  'гель для бровей',
  'vivienne sabo',
  'fixateur',
  'mossland',
  'planeta organica',
  'mixit',
]
const SEARCH_BRANDS = [
  'BANANA REPUBLIC',
  'CLARINS',
  'MOSCHINO',
  'CHOPARD',
  'PAYOT',
  'NEYDO',
  'KIKO MILANO',
  'ESSENCE',
  'PLANETA ORGANICA',
  'RAD',
  'DARLING',
  'VIVIENNE SABO',
]
const CYRILLIC_LETTERS = 'АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЭЮЯ'

function getBrandSectionTitle(brandName) {
  const first = brandName.trim().charAt(0).toUpperCase()
  if (!first) return '#'
  if (/\d/.test(first)) return '0-9'
  if (CYRILLIC_LETTERS.includes(first)) return first
  if (/[A-Z]/.test(first)) return first
  return '#'
}

function compareBrandSectionTitles(a, b) {
  if (a === '0-9') return -1
  if (b === '0-9') return 1
  if (a === '#') return 1
  if (b === '#') return -1
  return a.localeCompare(b, 'ru')
}

function buildBrandSections(brands) {
  const uniqueBrands = [...new Set((brands ?? []).map((item) => String(item).trim()).filter(Boolean))].sort((a, b) =>
    a.localeCompare(b, 'ru'),
  )
  const grouped = new Map()
  for (const brand of uniqueBrands) {
    const title = getBrandSectionTitle(brand)
    if (!grouped.has(title)) grouped.set(title, [])
    grouped.get(title).push(brand)
  }
  return [...grouped.entries()]
    .sort(([a], [b]) => compareBrandSectionTitles(a, b))
    .map(([title, items]) => ({ title, items }))
}

export default App
