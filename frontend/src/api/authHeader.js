const STORAGE_KEY = 'phrasenschwein.auth'

export function authHeader() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const auth = raw ? JSON.parse(raw) : null
    return auth ? { Authorization: `${auth.tokenType} ${auth.token}` } : {}
  } catch {
    return {}
  }
}