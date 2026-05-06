import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { createGiftCardOrder } from './api'

const GIFT_CARD_MAX_AMOUNT = 2000

export const GIFT_CARD_DESIGNS = [
  { id: 'card-2', title: 'Живописная груша', src: '/gift-cards/card-2.png' },
  { id: 'card-3', title: 'Скетч', src: '/gift-cards/card-3.png' },
  { id: 'card-4', title: 'Три цвета', src: '/gift-cards/card-4.png' },
  { id: 'card-5', title: 'SilverPear vibe', src: '/gift-cards/card-5.png' },
  { id: 'card-6', title: 'Лаванда', src: '/gift-cards/card-6.png' },
]

const GIFT_CARD_CACHE_BUST = {
  'card-6': '10',
}

/** Тот же слот/зум, что у «Лаванда» */
function isLavenderSlotDesign(id) {
  return id === 'card-2' || id === 'card-3' || id === 'card-4' || id === 'card-5' || id === 'card-6'
}

export function giftCardImageSrc(d) {
  const v = GIFT_CARD_CACHE_BUST[d.id]
  return v ? `${d.src}?v=${v}` : d.src
}

export function giftImageUrlForDesignId(designId) {
  const d = GIFT_CARD_DESIGNS.find((x) => x.id === designId)
  if (d) return giftCardImageSrc(d)
  return `/gift-cards/${designId}.png`
}

/** Маска +375 (29) 777-55-88 при вводе */
function formatBelarusPhoneInput(raw) {
  let digits = raw.replace(/\D/g, '')
  if (digits.length === 11 && digits.startsWith('80')) {
    digits = `375${digits.slice(2)}`
  }
  if (digits.startsWith('375')) {
    digits = digits.slice(3)
  }
  digits = digits.slice(0, 9)

  if (digits.length === 0) return '+375'
  if (digits.length === 1) return `+375 (${digits}`
  const op = digits.slice(0, 2)
  const rest = digits.slice(2)
  let s = `+375 (${op})`
  if (rest.length === 0) return s
  if (rest.length <= 3) return `${s} ${rest}`
  if (rest.length <= 5) return `${s} ${rest.slice(0, 3)}-${rest.slice(3)}`
  return `${s} ${rest.slice(0, 3)}-${rest.slice(3, 5)}-${rest.slice(5, 7)}`
}

function belarusPhoneDigits(raw) {
  let d = raw.replace(/\D/g, '')
  if (d.length === 11 && d.startsWith('80')) {
    d = `375${d.slice(2)}`
  }
  return d
}

export function GiftCardWizard({ open, onClose, onError, onSuccess }) {
  const [step, setStep] = useState(1)
  const [carouselIndex, setCarouselIndex] = useState(0)
  const [flippedById, setFlippedById] = useState({})
  const [amount, setAmount] = useState('50')
  const [recipientPhone, setRecipientPhone] = useState('+375')
  const [submitting, setSubmitting] = useState(false)
  const stripRef = useRef(null)
  const itemRefs = useRef([])
  const scrollRaf = useRef(null)

  const selected = GIFT_CARD_DESIGNS[carouselIndex] ?? GIFT_CARD_DESIGNS[0]

  const scrollToIndex = useCallback((i, behavior = 'smooth') => {
    const node = itemRefs.current[i]
    if (node) {
      node.scrollIntoView({ behavior, inline: 'center', block: 'nearest' })
    }
  }, [])

  const syncIndexFromScroll = useCallback(() => {
    const strip = stripRef.current
    if (!strip) return
    const center = strip.scrollLeft + strip.clientWidth / 2
    let bestIdx = 0
    let bestDist = Infinity
    itemRefs.current.forEach((node, i) => {
      if (!node) return
      const mid = node.offsetLeft + node.offsetWidth / 2
      const d = Math.abs(mid - center)
      if (d < bestDist) {
        bestDist = d
        bestIdx = i
      }
    })
    setCarouselIndex((prev) => (prev === bestIdx ? prev : bestIdx))
  }, [])

  const onStripScroll = () => {
    if (scrollRaf.current != null) cancelAnimationFrame(scrollRaf.current)
    scrollRaf.current = requestAnimationFrame(() => {
      scrollRaf.current = null
      syncIndexFromScroll()
    })
  }

  useEffect(() => {
    if (!open) return
    setCarouselIndex(0)
    requestAnimationFrame(() => {
      scrollToIndex(0, 'auto')
    })
  }, [open, scrollToIndex])

  const reset = useCallback(() => {
    setStep(1)
    setCarouselIndex(0)
    setFlippedById({})
    setAmount('50')
    setRecipientPhone('+375')
    setSubmitting(false)
  }, [])

  const handleClose = () => {
    reset()
    onClose()
  }

  const goPrev = () => {
    setCarouselIndex((i) => {
      const n = (i - 1 + GIFT_CARD_DESIGNS.length) % GIFT_CARD_DESIGNS.length
      requestAnimationFrame(() => scrollToIndex(n))
      return n
    })
  }

  const goNext = () => {
    setCarouselIndex((i) => {
      const n = (i + 1) % GIFT_CARD_DESIGNS.length
      requestAnimationFrame(() => scrollToIndex(n))
      return n
    })
  }

  const pickCard = (i) => {
    setCarouselIndex(i)
    requestAnimationFrame(() => scrollToIndex(i))
  }

  const toggleCardFlip = (designId) => {
    setFlippedById((prev) => ({ ...prev, [designId]: !prev[designId] }))
  }

  const onStripCardClick = (i, designId) => {
    pickCard(i)
    toggleCardFlip(designId)
  }

  const canSubmitAmount = useMemo(() => {
    const n = Number(amount.replace(',', '.'))
    return Number.isFinite(n) && n >= 1 && n <= GIFT_CARD_MAX_AMOUNT
  }, [amount])

  async function onConfirmSend() {
    if (!canSubmitAmount) {
      onError(`Укажите сумму от 1 до ${GIFT_CARD_MAX_AMOUNT} BYN`)
      return
    }
    const d = belarusPhoneDigits(recipientPhone.trim())
    if (d.length < 12 || !d.startsWith('375')) {
      onError('Введите полный номер: +375 (XX) XXX-XX-XX')
      return
    }
    try {
      setSubmitting(true)
      await createGiftCardOrder({
        designId: selected.id,
        amount: Number(String(amount).replace(',', '.')),
        recipientPhone: `+${d}`,
      })
      onSuccess?.()
      handleClose()
    } catch (e) {
      onError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) return null

  return (
    <div className="gift-wizard-backdrop" role="presentation" onClick={handleClose}>
      <div
        className="gift-wizard-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="gift-wizard-title"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="gift-wizard-close" aria-label="Закрыть" onClick={handleClose}>
          ×
        </button>
        <h2 id="gift-wizard-title" className="gift-wizard-title">
          Подарочная карта
        </h2>

        <ol className="gift-steps" aria-label="Этапы">
          <li className={step >= 1 ? 'active' : ''}>Дизайн</li>
          <li className={step >= 2 ? 'active' : ''}>Сумма</li>
          <li className={step >= 3 ? 'active' : ''}>Кому</li>
        </ol>

        {step === 1 && (
          <div className="gift-step-body">
            <div
              ref={stripRef}
              className="gift-strip"
              onScroll={onStripScroll}
              role="listbox"
              aria-label="Дизайны карт"
              aria-activedescendant={`gift-strip-opt-${carouselIndex}`}
            >
              {GIFT_CARD_DESIGNS.map((d, i) => (
                <button
                  key={d.id}
                  type="button"
                  id={`gift-strip-opt-${i}`}
                  role="option"
                  aria-selected={i === carouselIndex}
                  aria-pressed={Boolean(flippedById[d.id])}
                  aria-label={
                    flippedById[d.id] ? `Обратная сторона: ${d.title}. Нажмите, чтобы вернуть лицо.` : `${d.title}. Нажмите, чтобы перевернуть.`
                  }
                  ref={(el) => {
                    itemRefs.current[i] = el
                  }}
                  className={`gift-strip-item ${isLavenderSlotDesign(d.id) ? 'gift-strip-item--card6' : ''} ${i === carouselIndex ? 'is-selected' : ''}`}
                  data-card-id={d.id}
                  onClick={() => onStripCardClick(i, d.id)}
                >
                  <span className="gift-strip-flip">
                    <span className={`gift-strip-flip-inner ${flippedById[d.id] ? 'is-flipped' : ''}`}>
                      <span className="gift-strip-face gift-strip-face--front" aria-hidden="true">
                        <img
                          src={giftCardImageSrc(d)}
                          alt=""
                          className="gift-strip-img"
                          draggable={false}
                          loading="lazy"
                        />
                      </span>
                      <span className="gift-strip-face gift-strip-face--back" aria-hidden="true">
                        <span className="gift-strip-back-title">{d.title}</span>
                      </span>
                    </span>
                  </span>
                </button>
              ))}
            </div>
            <div className="gift-strip-nav gift-strip-nav--with-cta">
              <button type="button" className="gift-strip-arrow" onClick={goPrev} aria-label="Предыдущий дизайн">
                {'<'}
              </button>
              <button
                type="button"
                className="primary gift-step-next"
                aria-label="Далее: ввести сумму"
                onClick={() => setStep(2)}
              >
                Далее
              </button>
              <button type="button" className="gift-strip-arrow" onClick={goNext} aria-label="Следующий дизайн">
                {'>'}
              </button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="gift-step-body">
            <div className="gift-mini-preview">
              <div
                className={`gift-mini-preview-thumb ${isLavenderSlotDesign(selected.id) ? 'gift-mini-preview-thumb--card6' : ''}`}
                data-card-id={selected.id}
              >
                <img src={giftCardImageSrc(selected)} alt="" />
              </div>
            </div>
            <div className="gift-amount-stack">
              <label className="field-label gift-field-label--tight">
                Номинал (BYN)
                <input
                  type="number"
                  min="1"
                  max={GIFT_CARD_MAX_AMOUNT}
                  step="1"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="gift-amount-input"
                />
              </label>
              <p className="muted small gift-amount-hint">От 1 до 2000 BYN</p>
            </div>
            <div className="gift-step-actions">
              <button type="button" className="ghost" onClick={() => setStep(1)}>
                Назад
              </button>
              <button
                type="button"
                className="primary"
                disabled={!canSubmitAmount}
                aria-label="Далее: кому дарим"
                onClick={() => setStep(3)}
              >
                Далее
              </button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="gift-step-body">
            <div className="gift-mini-preview">
              <div
                className={`gift-mini-preview-thumb gift-mini-preview-thumb--sm ${isLavenderSlotDesign(selected.id) ? 'gift-mini-preview-thumb--card6' : ''}`}
                data-card-id={selected.id}
              >
                <img src={giftCardImageSrc(selected)} alt="" />
              </div>
              <div>
                <strong className="gift-step-summary-amount">
                  {Number(String(amount).replace(',', '.')) || '—'} BYN
                </strong>
              </div>
            </div>
            <label className="field-label">
              Телефон получателя
              <input
                type="tel"
                autoComplete="tel"
                inputMode="numeric"
                placeholder="+375 (29) 777-55-88"
                value={recipientPhone}
                onChange={(e) => setRecipientPhone(formatBelarusPhoneInput(e.target.value))}
                className="gift-phone-input"
              />
            </label>
            <div className="gift-step-actions">
              <button type="button" className="ghost" onClick={() => setStep(2)}>
                Назад
              </button>
              <button type="button" className="primary" disabled={submitting} onClick={onConfirmSend}>
                {submitting ? 'Отправка…' : 'Отправить карту'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
