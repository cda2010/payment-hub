import { ExecutionMode, Provider } from '../../core/model/api.models';

export interface CheckoutDraft {
  amount: number;
  currency: string;
  customerReference: string;
  phoneNumber: string;
  /** Origine facture (affichage checkout). */
  billSummary?: string | null;
  source?: 'checkout' | 'bills';
}

export type CheckoutPhase = 'order' | 'method' | 'execute' | 'result';

export function formatMoney(amount: number, currency: string): string {
  try {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency || 'XOF',
      maximumFractionDigits: 0
    }).format(amount);
  } catch {
    return `${amount} ${currency}`;
  }
}

export function modeLabel(mode: ExecutionMode): string {
  switch (mode) {
    case 'OTP':
      return 'Code OTP';
    case 'REDIRECT':
      return 'Redirection';
    case 'PUSH':
      return 'Notification';
    case 'USSD':
      return 'USSD';
    case 'DIRECT':
      return 'Direct';
    case 'ASYNC':
      return 'Asynchrone';
    default:
      return mode;
  }
}

export function methodTypeLabel(type: string | null | undefined): string {
  switch (type) {
    case 'CARD':
      return 'Carte bancaire';
    case 'BANK_TRANSFER':
      return 'Compte bancaire';
    case 'MOBILE_MONEY':
      return 'Mobile Money';
    case 'OTHER':
      return 'Services';
    default:
      return 'Autres';
  }
}

export function providerInitials(provider: Provider): string {
  return provider.libelle
    .split(/\s+/)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();
}

export function phaseIndex(phase: CheckoutPhase): number {
  switch (phase) {
    case 'order':
      return 0;
    case 'method':
      return 1;
    case 'execute':
      return 2;
    case 'result':
      return 3;
  }
}

export const CHECKOUT_STEPS = ['Récapitulatif', 'Moyen de paiement', 'Confirmation', 'Résultat'];

export const METHOD_TYPE_ORDER = ['MOBILE_MONEY', 'CARD', 'BANK_TRANSFER'] as const;
