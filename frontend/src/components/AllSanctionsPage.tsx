import { useEffect, useState } from 'react'
import { fetchAllSanctions, toggleLike, PhraseResponse } from '../api/sanctionApi'
import { formatDate } from '../utils/formatDate'

interface AllSanctionsPageProps {
  onBack: () => void
}

function AllSanctionsPage({ onBack }: AllSanctionsPageProps) {
  const [sanctions, setSanctions] = useState<PhraseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [likeError, setLikeError] = useState<string | null>(null)
  const [likingId, setLikingId] = useState<number | null>(null)

  function loadSanctions() {
    setLoading(true)
    fetchAllSanctions()
      .then((data) => {
        setSanctions(data)
        setError(null)
      })
      .catch((err) => {
        setError(err.message)
      })
      .finally(() => {
        setLoading(false)
      })
  }

  useEffect(() => {
    loadSanctions()
  }, [])

  async function handleLike(id: number) {
    setLikeError(null)
    setLikingId(id)
    try {
      await toggleLike(id)
      loadSanctions()
    } catch (err) {
      setLikeError(err.message)
    } finally {
      setLikingId(null)
    }
  }

  return (
    <div id="main">
      <button type="button" className="counter" onClick={onBack}>
        ← Zurück
      </button>
      <h2>Alle Sanktionen</h2>
      <div className="recent-sanctions all-sanctions">
        {loading && <p>Lade…</p>}
        {error && <p className="login-error">{error}</p>}
        {likeError && <p className="login-warning">{likeError}</p>}
        {!loading && !error && sanctions.length === 0 && <p>Noch keine Sanktionen.</p>}
        {!loading && !error && sanctions.length > 0 && (
          <ul className="recent-sanctions-list">
            {sanctions.map((sanction) => (
              <li key={sanction.id} className="recent-sanctions-item">
                <span className="recent-sanctions-receiver">{sanction.receiver}</span>
                <span className="recent-sanctions-text">{sanction.type}-Strafe</span>
                <span className="recent-sanctions-text">{sanction.text}</span>
                <span className="recent-sanctions-date">{formatDate(sanction.issuedAt)}</span>
                <button
                  type="button"
                  className={`like-button ${sanction.likedByCurrentUser ? 'liked' : ''}`}
                  onClick={() => handleLike(sanction.id)}
                  disabled={likingId === sanction.id}
                  aria-pressed={sanction.likedByCurrentUser}
                >
                  {sanction.likedByCurrentUser ? '♥' : '♡'} {sanction.likeCount}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export default AllSanctionsPage
