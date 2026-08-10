import { useEffect, useState } from 'react'
import { fetchRecentSanctions, PhraseResponse } from '../api/sanctionApi'

const dateFormatter = new Intl.DateTimeFormat('de-DE', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatDate(issuedAt: string) {
  const date = new Date(issuedAt)
  return Number.isNaN(date.getTime()) ? issuedAt : dateFormatter.format(date)
}

function RecentSanctionsList() {
  const [sanctions, setSanctions] = useState<PhraseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchRecentSanctions()
      .then((data) => {
        if (!cancelled) setSanctions(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    console.log(sanctions)
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="recent-sanctions">
      <h5>Letzte Sanktionen</h5>
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
  )
}

export default RecentSanctionsList
