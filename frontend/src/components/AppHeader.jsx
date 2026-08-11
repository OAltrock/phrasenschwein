import './AppHeader.css'

function AppHeader({ title, subtitle, onShowAllSanctions, onLogout }) {
  return (
    <header className="app-header">
      <div className="app-header-text">
        <h1>{title}</h1>
        {subtitle && <p className="app-header-subtitle">{subtitle}</p>}
      </div>
      <div className="app-header-actions">
        {onShowAllSanctions && (
          <button type="button" className="counter" onClick={onShowAllSanctions}>
            Alle Sanktionen
          </button>
        )}
        {onLogout && (
          <button type="button" className="counter" onClick={onLogout}>
            Abmelden
          </button>
        )}
      </div>
    </header>
  )
}

export default AppHeader
