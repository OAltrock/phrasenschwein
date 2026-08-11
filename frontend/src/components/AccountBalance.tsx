import { useEffect, useState } from 'react'
import { fetchCurrentUser } from '../api/usersApi'

const formatEuro = (amount: number) =>
  new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(amount)

interface AccountBalanceProps {
  refreshKey?: number | string
}

function AccountBalance({ refreshKey }: AccountBalanceProps = {}) {
  const [balance, setBalance] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchCurrentUser()
      .then((data) => {
        if (!cancelled) setBalance(Number(data.accountBalance))
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
    return () => {
      cancelled = true
    }
  }, [refreshKey])

  if (error) return null
  if (balance === null) return <span className="account-balance">Lade Kontostand…</span>

  return (
    <span className={`account-balance ${balance > 0 ? 'account-balance-due' : ''}`}>
      Kontostand: {formatEuro(balance)}
    </span>
  )
}

export default AccountBalance
