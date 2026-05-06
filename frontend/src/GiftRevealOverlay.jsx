import { GIFT_CARD_DESIGNS, giftCardImageSrc } from './GiftCardWizard'

function imageForDesignId(designId) {
  const d = GIFT_CARD_DESIGNS.find((x) => x.id === designId)
  if (d) return giftCardImageSrc(d)
  return `/gift-cards/${designId}.png`
}

function formatGiftAmount(amount) {
  const n = Number(amount)
  if (!Number.isFinite(n)) return String(amount)
  return (
    n.toLocaleString('ru-RU', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + ' руб.'
  )
}

const SPARKLE_COUNT = 28

export function GiftRevealOverlay({ gift, onDismiss }) {
  if (!gift) return null

  const imgSrc = imageForDesignId(gift.designId)

  return (
    <div
      className="gift-reveal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="gift-reveal-title"
      onClick={onDismiss}
    >
      <div className="gift-reveal-modal" onClick={(e) => e.stopPropagation()}>
        <div className="gift-reveal-sparkles" aria-hidden>
          {Array.from({ length: SPARKLE_COUNT }, (_, i) => (
            <span
              key={i}
              className="gift-reveal-sparkle"
              style={{
                left: `${6 + (i * 37) % 78}%`,
                top: `${8 + (i * 29) % 72}%`,
                animationDelay: `${i * 0.085}s`,
              }}
            />
          ))}
        </div>
        <div className="gift-reveal-glow" aria-hidden />
        <h2 id="gift-reveal-title" className="gift-reveal-title">
          Вам подарили карту!
        </h2>
        <p className="gift-reveal-sub">Номинал зачислится на ваш аккаунт по номеру телефона.</p>
        <div className="gift-reveal-card-wrap">
          <div className="gift-reveal-card-float">
            <img src={imgSrc} alt="" className="gift-reveal-card-img" />
          </div>
        </div>
        <p className="gift-reveal-amount">{formatGiftAmount(gift.amount)}</p>
        <button type="button" className="primary gift-reveal-cta" onClick={onDismiss}>
          Супер, спасибо!
        </button>
      </div>
    </div>
  )
}
