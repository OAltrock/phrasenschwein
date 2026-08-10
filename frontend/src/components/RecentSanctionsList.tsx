import { useEffect, useState } from "react";
import { fetchRecentSanctions, PhraseResponse } from "../api/sanctionApi";
import { formatDate } from "../utils/formatDate";

interface RecentSanctionsListProps {
  onShowAll: () => void;
}

function RecentSanctionsList({ onShowAll }: RecentSanctionsListProps) {
  const [sanctions, setSanctions] = useState<PhraseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchRecentSanctions()
      .then((data) => {
        if (!cancelled) setSanctions(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div style={{flexGrow:'2', border: '2px solid #371952', }}>
      <h5>Letzte Sanktionen</h5>
      <div
        className="recent-sanctions"
        style={{
          display: "flex",
          flexDirection: "row",
          maxHeight: "80vh",
          overflowY: "auto",
          minWidth: "50vw",          
          padding: '3px',
        }}
      >
        {loading && <p>Lade…</p>}
        {error && <p className="login-error">{error}</p>}
        {!loading && !error && sanctions.length === 0 && (
          <p>Noch keine Sanktionen.</p>
        )}
        {!loading && !error && sanctions.length > 0 && (
          <ul className="recent-sanctions-list">
            {sanctions.map((sanction) => (
              <li key={sanction.id} className="recent-sanctions-item">
                <span className="recent-sanctions-receiver">
                  {sanction.receiver}
                </span>
                <span className="recent-sanctions-text">
                  {sanction.type}-Strafe
                </span>
                <span className="recent-sanctions-text">{sanction.text}</span>
                <span className="recent-sanctions-date">
                  {formatDate(sanction.issuedAt)}
                </span>
              </li>
            ))}
          </ul>
        )}
        <button
          type="button"
          className="counter"
          onClick={onShowAll}
          style={{ maxHeight: "9vh", naxWidth: '12vw', margin: '0 1vw', position: 'sticky', top: '1', alignSelf: 'flex-start' }}
        >
          Alle Sanktionen anzeigen
        </button>
      </div>
    </div>
  );
}

export default RecentSanctionsList;
