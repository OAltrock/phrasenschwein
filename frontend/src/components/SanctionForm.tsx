import { useState } from 'react'
import FineTypeSelect, { FineType } from './FineTypeSelect'
import { sanction } from '../api/sanctionApi'
import UserSelect from './UserSelect'

function SanctionForm() {
  const [username, setUsername] = useState<string | null>(null)
  const [text, setText] = useState('')
  const [fineType, setFineType] = useState<FineType | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (!username || !fineType) {
      setError('Bitte Nutzer und Strafart auswählen.')
      return
    }

    setIsSubmitting(true)
    try {
      // TODO: wire up once a sanction/phrase endpoint exists on the backend
      console.log({ username, text, fineType })
      const resp = await sanction( username, fineType, text)
      console.log(resp)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h5>Jemand hat eine hole Phrase benutzt?</h5>
      <UserSelect value={username} onChange={setUsername} required />
      <FineTypeSelect value={fineType} onChange={setFineType} required />
      <label htmlFor="text">Grund</label>
      <input
        id="text"
        type="text"
        value={text}
        onChange={(event) => setText(event.target.value)}
      />
      {error && <p className="login-error">{error}</p>}
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Submitting…' : 'Sanktioniere'}
      </button>
    </form>
  )
}

export default SanctionForm
