import { useState } from 'react'
import { createUser } from '../api/usersApi'

interface AdminPageProps {
  adminUsername: string
  onLogout: () => void
}

function AdminPage({ adminUsername, onLogout }: AdminPageProps) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setMessage(null)

    setIsSubmitting(true)
    try {
      const created = await createUser(username, password)
      setMessage(`Nutzer "${created.username}" wurde angelegt.`)
      setUsername('')
      setPassword('')
    } catch (err) {
      setError(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div id="main">
      <h1>Willkommen, {adminUsername}</h1>
      <p>Du bist als Admin eingeloggt.</p>
      <section id="center">
        <form className="login-form" onSubmit={handleSubmit}>
          <h5>Nutzer hinzufügen</h5>
          <label htmlFor="new-username">Username</label>
          <input
            id="new-username"
            type="text"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="off"
            required
          />
          <label htmlFor="new-password">Passwort</label>
          <input
            id="new-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            minLength={8}
            required
          />
          {error && <p className="login-error">{error}</p>}
          {message && <p>{message}</p>}
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Lege an…' : 'Nutzer anlegen'}
          </button>
        </form>
      </section>
      <button type="button" className="counter" onClick={onLogout} style={{ marginTop: '15px' }}>
        Log out
      </button>
    </div>
  )
}

export default AdminPage
