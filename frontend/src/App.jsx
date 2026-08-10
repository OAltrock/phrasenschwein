import { useState } from 'react'
import LoginForm from './components/LoginForm'
import SanctionForm from './components/SanctionForm'
import './App.css'

const STORAGE_KEY = 'phrasenschwein.auth'

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)    
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function App() {
  const [auth, setAuth] = useState(loadStoredAuth)

  function handleLoginSuccess(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    setAuth(data)
  }

  function handleLogout() {
    localStorage.removeItem(STORAGE_KEY)
    setAuth(null)
  }

  if (!auth) {    
    return (
      <section id="center">
        <LoginForm onLoginSuccess={handleLoginSuccess} />
      </section>
    )
  }

  console.log(JSON.parse(localStorage.getItem(STORAGE_KEY)))

  return (       
    <section id="center">
      <h1>Welcome, {auth.username}</h1>
      <p>You are logged in.</p>
      <SanctionForm />
      <button type="button" className="counter" onClick={handleLogout}>
        Log out
      </button>
    </section>
  )
}

export default App
