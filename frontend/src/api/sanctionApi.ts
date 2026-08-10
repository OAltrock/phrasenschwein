import { authHeader } from './authHeader'
import { FineType } from '../components/FineTypeSelect'

export interface SanctionRequest {
  username: string
  type: FineType
  text: string
}

export interface PhraseResponse {
  id: number
  issuer: string
  receiver: string
  type: FineType
  amount: number
  text: string
  issuedAt: string
}

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export async function sanction(username: string, type: FineType, text: string) {
  const response = await fetch(`${API_URL}/api/phrase/sanction`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ username, type, text } satisfies SanctionRequest),
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || 'Request failed')
  }

  return response.json()
}

export async function fetchRecentSanctions(): Promise<PhraseResponse[]> {
  const response = await fetch(`${API_URL}/api/phrase/recent`)

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || 'Failed to load recent sanctions')
  }

  return response.json()
}

export async function fetchAllSanctions(): Promise<PhraseResponse[]> {
  const response = await fetch(`${API_URL}/api/phrase/all`)

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || 'Failed to load sanctions')
  }

  return response.json()
}
