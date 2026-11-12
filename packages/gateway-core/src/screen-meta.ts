import type { IntegrationScreen } from './types';

export function screenPaymentId(screen: IntegrationScreen | null | undefined): string | null {
  const id = screen?.meta?.['paymentId'];
  return typeof id === 'string' && id.trim() ? id : null;
}

export function screenStatus(screen: IntegrationScreen | null | undefined): string | null {
  const status = screen?.meta?.['status'];
  return typeof status === 'string' && status.trim() ? status : null;
}

/**
 * Statuts où payment-2 attend encore une action client — reprise via
 * `GET /gateway/payments/{id}`.
 */
const RESUMABLE_STATUSES = new Set([
  'AWAITING_USER_INPUT',
  'OTP_REQUIRED',
  'RETURNED_FOR_CORRECTION'
]);

export function isResumableStatus(statusCode?: string | null): boolean {
  return RESUMABLE_STATUSES.has((statusCode || '').toUpperCase());
}

export function cloneScreen(screen: IntegrationScreen): IntegrationScreen {
  try {
    return structuredClone(screen);
  } catch {
    return JSON.parse(JSON.stringify(screen)) as IntegrationScreen;
  }
}
