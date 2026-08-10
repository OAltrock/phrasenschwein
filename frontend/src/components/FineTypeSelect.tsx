import { useState, useRef, useEffect, useId } from 'react'
import { ChevronDown, Check } from 'lucide-react'

export enum FineType {
  LEICHT = 'LEICHT',
  STANDARD = 'STANDARD',
  SCHWER = 'SCHWER',
}

interface FineTypeOption {
  value: FineType
  label: string
  amount: number
}

const FINE_TYPE_OPTIONS: FineTypeOption[] = [
  { value: FineType.LEICHT, label: 'Leicht', amount: 0.5 },
  { value: FineType.STANDARD, label: 'Standard', amount: 1.0 },
  { value: FineType.SCHWER, label: 'Schwer', amount: 2.0 },
]

const formatEuro = (amount: number) =>
  new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(amount)

interface FineTypeSelectProps {
  value: FineType | null
  onChange: (value: FineType) => void
  label?: string
  disabled?: boolean
  required?: boolean
}

export default function FineTypeSelect({
  value,
  onChange,
  label = 'Wie hohl war sie?',
  disabled = false,
  required = false,
}: FineTypeSelectProps) {
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(0)
  const containerRef = useRef<HTMLDivElement>(null)
  const listRef = useRef<HTMLUListElement>(null)
  const listboxId = useId()

  const selected = FINE_TYPE_OPTIONS.find((o) => o.value === value) ?? null

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
      const idx = selected ? FINE_TYPE_OPTIONS.findIndex((o) => o.value === selected.value) : 0
      setActiveIndex(idx === -1 ? 0 : idx)
    }
  }, [open]) // eslint-disable-line react-hooks/exhaustive-deps

  function commit(index: number) {
    onChange(FINE_TYPE_OPTIONS[index].value)
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
        setActiveIndex((i) => Math.min(i + 1, FINE_TYPE_OPTIONS.length - 1))
        break
      case 'ArrowUp':
        e.preventDefault()
        setActiveIndex((i) => Math.max(i - 1, 0))
        break
      case 'Enter':
      case ' ':
        e.preventDefault()
        commit(activeIndex)
        break
      case 'Escape':
        e.preventDefault()
        setOpen(false)
        break
    }
  }

  return (
    <div className="fine-type-select" ref={containerRef}>
      <style>{`
        .fine-type-select {
          --border: #d8d5cc;
          --border-hover: #b8b4a8;
          --accent: #2f6f5e;
          --accent-soft: #eaf2ef;
          --text: #23221f;
          --text-muted: #6b6862;
          --bg: #ffffff;
          position: relative;
          width: 260px;
          font-family: ui-sans-serif, system-ui, -apple-system, sans-serif;
        }
        .fts-label {
          display: block;
          font-size: 12.5px;
          font-weight: 600;
          letter-spacing: 0.02em;
          color: var(--text-muted);
          margin-bottom: 6px;
        }
        .fts-required {
          color: #c0392b;
        }
        .fts-trigger {
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
          color: var(--text);
          cursor: pointer;
          transition: border-color 0.15s ease, box-shadow 0.15s ease;
        }
        .fts-trigger:hover:not(:disabled) {
          border-color: var(--border-hover);
        }
        .fts-trigger:focus-visible {
          outline: none;
          border-color: var(--accent);
          box-shadow: 0 0 0 3px var(--accent-soft);
        }
        .fts-trigger:disabled {
          opacity: 0.55;
          cursor: not-allowed;
        }
        .fts-trigger-left {
          display: flex;
          align-items: baseline;
          gap: 8px;
          min-width: 0;
        }
        .fts-placeholder {
          color: var(--text-muted);
        }
        .fts-amount {
          font-variant-numeric: tabular-nums;
          font-size: 12.5px;
          color: var(--text-muted);
        }
        .fts-chevron {
          flex-shrink: 0;
          color: var(--text-muted);
          transition: transform 0.15s ease;
        }
        .fts-chevron.open {
          transform: rotate(180deg);
        }
        .fts-listbox {
          position: absolute;
          z-index: 20;
          top: calc(100% + 6px);
          left: 0;
          right: 0;
          background: var(--bg);
          border: 1.5px solid var(--border);
          border-radius: 10px;
          box-shadow: 0 8px 24px rgba(20, 20, 18, 0.1);
          padding: 4px;
          list-style: none;
          margin: 0;
          max-height: 240px;
          overflow-y: auto;
        }
        .fts-option {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 10px;
          padding: 9px 10px;
          border-radius: 7px;
          font-size: 14.5px;
          color: var(--text);
          cursor: pointer;
        }
        .fts-option.active {
          background: var(--accent-soft);
        }
        .fts-option-left {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .fts-option-amount {
          font-variant-numeric: tabular-nums;
          font-size: 12.5px;
          color: var(--text-muted);
        }
        .fts-check {
          color: var(--accent);
          flex-shrink: 0;
        }
        .fts-check-spacer {
          width: 14px;
          flex-shrink: 0;
        }
      `}</style>

      <label className="fts-label" id={`${listboxId}-label`}>
        {label}
        {required && <span className="fts-required"> *</span>}
      </label>

      <button
        type="button"
        className="fts-trigger"
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-required={required}
        aria-labelledby={`${listboxId}-label`}
        disabled={disabled}
        onClick={() => setOpen((o) => !o)}
        onKeyDown={onKeyDown}
      >
        <span className="fts-trigger-left">
          {selected ? (
            <>
              <span>{selected.label}</span>
              <span className="fts-amount">{formatEuro(selected.amount)}</span>
            </>
          ) : (
            <span className="fts-placeholder">Strafart wählen</span>
          )}
        </span>
        <ChevronDown size={16} className={`fts-chevron ${open ? 'open' : ''}`} />
      </button>

      {open && (
        <ul
          className="fts-listbox"
          role="listbox"
          ref={listRef}
          aria-labelledby={`${listboxId}-label`}
        >
          {FINE_TYPE_OPTIONS.map((option, index) => {
            const isSelected = selected?.value === option.value
            const isActive = index === activeIndex
            return (
              <li
                key={option.value}
                role="option"
                aria-selected={isSelected}
                className={`fts-option ${isActive ? 'active' : ''}`}
                onMouseEnter={() => setActiveIndex(index)}
                onClick={() => commit(index)}
              >
                <span className="fts-option-left">
                  {isSelected ? (
                    <Check size={14} className="fts-check" />
                  ) : (
                    <span className="fts-check-spacer" />
                  )}
                  {option.label}
                </span>
                <span className="fts-option-amount">{formatEuro(option.amount)}</span>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
