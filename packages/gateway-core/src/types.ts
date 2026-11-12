/** Contrat Product Gateway `/api/v1/gateway` — aligné payment-2. */

import type { RfDataSourceConfig, RfShowWhen } from './rf-datasource';

export type IntegrationScreenType =
  | 'FORM'
  | 'ENTER_OTP'
  | 'WAIT'
  | 'REDIRECT'
  | 'DISPLAY_MESSAGE'
  | 'SUCCESS'
  | 'FAILURE'
  | string;

export interface IntegrationScreenAction {
  id: string;
  label?: string;
  style?: string;
}

export interface IntegrationScreen {
  type: IntegrationScreenType;
  title?: string | null;
  message?: string | null;
  formKey?: string | null;
  formSchema?: Record<string, unknown> | null;
  fields?: Array<Record<string, unknown>> | null;
  data?: Record<string, unknown> | null;
  actions?: IntegrationScreenAction[] | null;
  otpLength?: number | null;
  url?: string | null;
  pollAfterSeconds?: number | null;
  meta?: Record<string, unknown> | null;
}

/** Champ UI plat pour rendu natif (Ionic / PrimeNG / …). */
export type GatewayFieldType =
  | 'TEXT'
  | 'NUMBER'
  | 'MONEY'
  | 'PHONE'
  | 'EMAIL'
  | 'DATE'
  | 'SELECT'
  | 'CHECKBOX'
  | string;

export interface GatewayFieldOption {
  label: string;
  value: string;
  icon?: string;
  sub?: string;
}

export interface GatewayField {
  code: string;
  label: string;
  type: GatewayFieldType;
  required?: boolean;
  placeholder?: string;
  options?: GatewayFieldOption[];
  min?: number;
  max?: number;
  pattern?: string;
  rfDataSource?: RfDataSourceConfig | null;
  showWhen?: RfShowWhen | null;
  amountChips?: number[] | null;
}

/** Champ produit facturier (lookup / confirm / amount). */
export interface GatewayProductField {
  name: string;
  label?: string | null;
  type?: string | null;
  required?: boolean;
  placeholder?: string | null;
  pattern?: string | null;
  role?: string | null;
  options?: string[] | null;
  defaultValue?: string | null;
  [key: string]: unknown;
}

/**
 * POST /gateway/products/{code} — enveloppe de démarrage commune.
 * Toute clé non réservée est versée dans le contexte paiement.
 */
export interface StartProductRequest {
  provider?: string;
  destinationProvider?: string;
  amount?: number;
  currency?: string;
  customerReference?: string;
  phoneNumber?: string;
  idempotencyKey?: string;
  channel?: string;
  accessChannel?: string;
  audience?: string;
  /** Alias audience (compat clients corporate / legacy). */
  productAudience?: string;
  [key: string]: unknown;
}

/** Hooks UI injectés dans {@link Payment2Client.run}. */
export interface GatewayUi {
  collectForm(screen: IntegrationScreen): Promise<Record<string, unknown>>;
  collectOtp(screen: IntegrationScreen): Promise<string>;
  openUrl(url: string): void;
  showWaiting(message?: string | null): void;
}

export interface Payment2ClientDefaults {
  channel: string;
  audience: string;
  acceptLanguage?: string;
}
