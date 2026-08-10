import { useState } from 'react'
import { resetAccount } from '../api/usersApi'
import UserSelect from './UserSelect'

interface AccountResetFormProps {
  currentUsername?: string
  refreshKey?: number | string
}

function AccountResetForm({ currentUsername, refreshKey }: AccountResetFormProps = {}) {
  const [username, setUsername] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setMessage(null)

    if (!username) {
      setError('Bitte Nutzer auswählen.')
      return
    }

    setIsSubmitting(true)
    try {
      const resp = await resetAccount(username)
      setMessage(`Konto von ${resp.username} wurde auf 0 zurückgesetzt.`)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h5>Konto eines Nutzers zurücksetzen</h5>
      <UserSelect
        value={username}
        onChange={setUsername}
        label="Welches Konto?"
        required
        excludeUsername={currentUsername}
        refreshKey={refreshKey}
      />
      {error && <p className="login-error">{error}</p>}
      {message && <p>{message}</p>}
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Setze zurück…' : 'Konto auf 0 setzen'}
      </button>
    </form>
  )
}

export default AccountResetForm
