import { useState, useRef, useEffect, useId } from 'react'
import { ChevronDown, Check } from 'lucide-react'
import { fetchUsers } from '../api/usersApi'

interface UserOption {
  id: number
  username: string
}

interface UserSelectProps {
  value: string | null
  onChange: (username: string) => void
  label?: string
  disabled?: boolean
  required?: boolean
  excludeUsername?: string | null
  refreshKey?: number | string
}

export default function UserSelect({
  value,
  onChange,
  label = 'Wer war es?',
  disabled = false,
  required = false,
  excludeUsername = null,
  refreshKey,
}: UserSelectProps) {
  const [users, setUsers] = useState<UserOption[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(0)
  const containerRef = useRef<HTMLDivElement>(null)
  const listRef = useRef<HTMLUListElement>(null)
  const listboxId = useId()

  const selectableUsers = excludeUsername
    ? users.filter((u) => u.username !== excludeUsername)
    : users

  const selected = selectableUsers.find((u) => u.username === value) ?? null

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchUsers()
      .then((data) => {
        if (!cancelled) setUsers(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [refreshKey])

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', onClickOutside)
    return () => document.removeEventListener('mousedown', onClickOutside)
  }, [])

  useEffect(() => {
    if (open) {
      const idx = selected ? selectableUsers.findIndex((u) => u.username === selected.username) : 0
      setActiveIndex(idx === -1 ? 0 : idx)
    }
  }, [open]) // eslint-disable-line react-hooks/exhaustive-deps

  function commit(index: number) {
    onChange(selectableUsers[index].username)
    setOpen(false)
  }

  function onKeyDown(e: React.KeyboardEvent) {
    if (!open) {
      if (e.key === 'Enter' || e.key === ' ' || e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        e.preventDefault()
        setOpen(true)
      }
      return
    }
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault()
        setActiveIndex((i) => Math.min(i + 1, selectableUsers.length - 1))
        break
      case 'ArrowUp':
        e.preventDefault()
        setActiveIndex((i) => Math.max(i - 1, 0))
        break
      case 'Enter':
      case ' ':
        e.preventDefault()
        if (selectableUsers.length > 0) commit(activeIndex)
        break
      case 'Escape':
        e.preventDefault()
        setOpen(false)
        break
    }
  }

  const isDisabled = disabled || loading || !!error || selectableUsers.length === 0

  return (
    <div className="user-select" ref={containerRef}>
      <style>{`
        .user-select {
          position: relative;
          width: 260px;
          font-family: var(--sans);
        }
        .us-label {
          display: block;
          font-size: 12.5px;
          font-weight: 600;
          letter-spacing: 0.02em;
          color: var(--text);
          margin-bottom: 6px;
        }
        .us-required {
          color: #c0392b;
        }
        .us-trigger {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 8px;
          background: var(--bg);
          border: 1.5px solid var(--border);
          border-radius: 10px;
          padding: 10px 12px;
          font-size: 14.5px;
          color: var(--text-h);
          cursor: pointer;
          transition: border-color 0.15s ease, box-shadow 0.15s ease;
        }
        .us-trigger:hover:not(:disabled) {
          border-color: var(--accent-border);
        }
        .us-trigger:focus-visible {
          outline: none;
          border-color: var(--accent);
          box-shadow: 0 0 0 3px var(--accent-bg);
        }
        .us-trigger:disabled {
          opacity: 0.55;
          cursor: not-allowed;
        }
        .us-placeholder {
          color: var(--text);
        }
        .us-chevron {
          flex-shrink: 0;
          color: var(--text);
          transition: transform 0.15s ease;
        }
        .us-chevron.open {
          transform: rotate(180deg);
        }
        .us-listbox {
          position: absolute;
          z-index: 20;
          top: calc(100% + 6px);
          left: 0;
          right: 0;
          background: var(--bg);
          border: 1.5px solid var(--border);
          border-radius: 10px;
          box-shadow: var(--shadow);
          padding: 4px;
          list-style: none;
          margin: 0;
          max-height: 240px;
          overflow-y: auto;
        }
        .us-option {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 9px 10px;
          border-radius: 7px;
          font-size: 14.5px;
          color: var(--text-h);
          cursor: pointer;
        }
        .us-option.active {
          background: var(--accent-bg);
        }
        .us-check {
          color: var(--accent);
          flex-shrink: 0;
        }
        .us-check-spacer {
          width: 14px;
          flex-shrink: 0;
        }
      `}</style>

      <label className="us-label" id={`${listboxId}-label`}>
        {label}
        {required && <span className="us-required"> *</span>}
      </label>

      <button
        type="button"
        className="us-trigger"
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-required={required}
        aria-labelledby={`${listboxId}-label`}
        disabled={isDisabled}
        onClick={() => setOpen((o) => !o)}
        onKeyDown={onKeyDown}
      >
        <span>
          {loading ? (
            <span className="us-placeholder">Lade Nutzer…</span>
          ) : error ? (
            <span className="us-placeholder">{error}</span>
          ) : selected ? (
            selected.username
          ) : (
            <span className="us-placeholder">Nutzer wählen</span>
          )}
        </span>
        <ChevronDown size={16} className={`us-chevron ${open ? 'open' : ''}`} />
      </button>

      {open && !isDisabled && (
        <ul
          className="us-listbox"
          role="listbox"
          ref={listRef}
          aria-labelledby={`${listboxId}-label`}
        >
          {selectableUsers.map((user, index) => {
            const isSelected = selected?.username === user.username
            const isActive = index === activeIndex
            return (
              <li
                key={user.id}
                role="option"
                aria-selected={isSelected}
                className={`us-option ${isActive ? 'active' : ''}`}                
                onMouseEnter={() => setActiveIndex(index)}
                onClick={() => commit(index)}
              >
                {isSelected ? (
                  <Check size={14} className="us-check" />
                ) : (
                  <span className="us-check-spacer" />
                )}
                {user.username}
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
