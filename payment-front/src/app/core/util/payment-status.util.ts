/**
 * Libellés FR des statuts paiement — console ops
 * (l’historique canal préfère {@code statusLabel} renvoyé par le gateway).
 */
const STATUS_LABELS: Record<string, string> = {
  CREATED: 'Créée',
  INITIATED: 'Initiée',
  PENDING: 'En cours',
  PROCESSING: 'En cours',
  OTP_REQUIRED: 'Code à saisir',
  AWAITING_USER_INPUT: 'En attente',
  WAITING_APPROVAL: 'En validation',
  RETURNED_FOR_CORRECTION: 'À corriger',
  SUCCESS: 'Validée',
  FAILED: 'Échouée',
  CANCELLED: 'Annulée',
  EXPIRED: 'Expirée',
};

export type PaymentStatusSeverity = 'success' | 'info' | 'warning' | 'danger' | 'secondary';

export function paymentStatusLabel(status?: string | null, fallbackLabel?: string | null): string {
  const code = (status || '').trim().toUpperCase();
  if (fallbackLabel?.trim()) {
    return fallbackLabel.trim();
  }
  if (!code) {
    return '—';
  }
  return STATUS_LABELS[code] || status!.trim();
}

export function paymentStatusSeverity(status?: string | null): PaymentStatusSeverity {
  switch ((status || '').trim().toUpperCase()) {
    case 'SUCCESS':
      return 'success';
    case 'FAILED':
    case 'CANCELLED':
    case 'EXPIRED':
      return 'danger';
    case 'WAITING_APPROVAL':
    case 'AWAITING_USER_INPUT':
    case 'OTP_REQUIRED':
    case 'RETURNED_FOR_CORRECTION':
      return 'warning';
    case 'PROCESSING':
    case 'PENDING':
    case 'INITIATED':
    case 'CREATED':
      return 'info';
    default:
      return 'secondary';
  }
}

/** Options dropdown statut (filtre) — label humanisé, value = code API. */
export function paymentStatusFilterOptions(
  codes: readonly string[]
): { label: string; value: string }[] {
  return [
    { label: 'Tous', value: '' },
    ...codes.map((value) => ({ label: paymentStatusLabel(value), value })),
  ];
}

/** `yyyy-MM-dd` → Instant ISO début / fin de journée UTC. */
export function dateInputToInstant(date: string, endOfDay: boolean): string | undefined {
  const d = (date || '').trim();
  if (!/^\d{4}-\d{2}-\d{2}$/.test(d)) {
    return undefined;
  }
  return endOfDay ? `${d}T23:59:59.999Z` : `${d}T00:00:00.000Z`;
}
