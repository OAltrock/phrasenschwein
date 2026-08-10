import { useEffect, useRef, useState } from 'react'
import FineTypeSelect, { FineType } from './FineTypeSelect'
import { sanction } from '../api/sanctionApi'
import UserSelect from './UserSelect'

const MESSAGE_DURATION_MS = 5000

function SanctionForm() {
  const [username, setUsername] = useState<string | null>(null)
  const [text, setText] = useState('')
  const [fineType, setFineType] = useState<FineType | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const messageTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (messageTimeoutRef.current) clearTimeout(messageTimeoutRef.current)
    }
  }, [])

  function showMessage(text: string) {
    if (messageTimeoutRef.current) clearTimeout(messageTimeoutRef.current)
    setMessage(text)
    messageTimeoutRef.current = setTimeout(() => setMessage(null), MESSAGE_DURATION_MS)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (!username || !fineType) {
      setError('Bitte Nutzer und Strafart auswählen.')
      return
    }

    setIsSubmitting(true)
    try {
      console.log(fineType)
      const resp = await sanction( username, fineType, text)
      console.log(resp)
      showMessage(`Auf dem Konto von ${resp.receiver} wurde wurde eine ${fineType.toLowerCase()} Strafe erhoben.`)
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
      {message && <p>{message}</p>}
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Submitting…' : 'Sanktioniere'}
      </button>      
    </form>
  )
}

export default SanctionForm
