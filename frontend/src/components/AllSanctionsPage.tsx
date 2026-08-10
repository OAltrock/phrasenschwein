import { useEffect, useState } from 'react'
import { fetchAllSanctions, PhraseResponse } from '../api/sanctionApi'
import { formatDate } from '../utils/formatDate'

interface AllSanctionsPageProps {
  onBack: () => void
}

function AllSanctionsPage({ onBack }: AllSanctionsPageProps) {
  const [sanctions, setSanctions] = useState<PhraseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchAllSanctions()
      .then((data) => {
        if (!cancelled) setSanctions(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div id="main">
      <button type="button" className="counter" onClick={onBack}>
        ← Zurück zum Login
      </button>
      <h2>Alle Sanktionen</h2>
      <div className="recent-sanctions all-sanctions">
        {loading && <p>Lade…</p>}
        {error && <p className="login-error">{error}</p>}
        {!loading && !error && sanctions.length === 0 && <p>Noch keine Sanktionen.</p>}
        {!loading && !error && sanctions.length > 0 && (
          <ul className="recent-sanctions-list">
            {sanctions.map((sanction) => (
              <li key={sanction.id} className="recent-sanctions-item">
                <span className="recent-sanctions-receiver">{sanction.receiver}</span>
                <span className="recent-sanctions-text">{sanction.type}-Strafe</span>
                <span className="recent-sanctions-text">{sanction.text}</span>
                <span className="recent-sanctions-date">{formatDate(sanction.issuedAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export default AllSanctionsPage
