const dateFormatter = new Intl.DateTimeFormat('de-DE', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export function formatDate(issuedAt: string) {
  const date = new Date(issuedAt)
  return Number.isNaN(date.getTime()) ? issuedAt : dateFormatter.format(date)
}
