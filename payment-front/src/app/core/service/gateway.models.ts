/** Contrat `/api/v1/gateway` — IntegrationScreenResponse. */

import type {
  IntegrationScreen,
  IntegrationScreenAction,
  IntegrationScreenType
} from '@payment2/gateway-core';

export type {
  IntegrationScreen,
  IntegrationScreenAction,
  IntegrationScreenType
};

export { screenPaymentId, screenStatus, isResumableStatus } from '@payment2/gateway-core';

/** Canal d'accès — aligné backend {@code AccessChannel}. */
export type AccessChannel = 'MOBILE' | 'WEB' | 'GAB' | 'USSD' | 'BACKOFFICE';

export const ACCESS_CHANNELS: ReadonlyArray<{ code: AccessChannel; label: string; hint: string }> = [
  { code: 'MOBILE', label: 'Mobile', hint: 'App native / Capacitor' },
  { code: 'WEB', label: 'Web', hint: 'Portail web client' },
  { code: 'GAB', label: 'GAB', hint: 'Guichet automatique' },
  { code: 'USSD', label: 'USSD', hint: 'Menu USSD' },
  { code: 'BACKOFFICE', label: 'Back-office', hint: 'Agent / agence' },
];

/** Audience produit — aligné backend {@code ProductAudience}. */
export type ProductAudience = 'RETAIL' | 'CORPORATE' | 'AGENT' | 'MERCHANT' | 'BACKOFFICE';

export const PRODUCT_AUDIENCES: ReadonlyArray<{
  code: ProductAudience;
  label: string;
  hint: string;
}> = [
  { code: 'RETAIL', label: 'Retail', hint: 'Particuliers' },
  { code: 'CORPORATE', label: 'Corporate', hint: 'Entreprises' },
  { code: 'AGENT', label: 'Agent', hint: 'Réseau d’agents' },
  { code: 'MERCHANT', label: 'Marchand', hint: 'Till / encaissement' },
  { code: 'BACKOFFICE', label: 'Back-office', hint: 'Outils banque' },
];

export interface ProductJourneyMeta {
  journeyCode?: string | null;
  supportedChannels: AccessChannel[];
  defaultChannel: AccessChannel | null;
  supportedAudiences: ProductAudience[];
  defaultAudience: ProductAudience | null;
}

/** Fallback journey si le casting catalogue ne le fournit pas. */
export function defaultJourneyCode(productCode: string): string | undefined {
  const code = productCode.trim().toUpperCase();
  if (code === 'CASH_OUT') {
    return 'STANDARD_CASH_OUT';
  }
  if (code === 'CASH_IN') {
    return 'STANDARD_CASH_IN';
  }
  if (code === 'CHECKOUT' || code === 'PAYEMENT' || code === 'PAYMENT') {
    return 'STANDARD_CHECKOUT';
  }
  return undefined;
}

export function parseAccessChannel(raw: unknown): AccessChannel | null {
  if (typeof raw !== 'string') {
    return null;
  }
  const code = raw.trim().toUpperCase();
  return ACCESS_CHANNELS.some((c) => c.code === code) ? (code as AccessChannel) : null;
}

export function parseAccessChannels(raw: unknown): AccessChannel[] {
  if (!Array.isArray(raw)) {
    return [];
  }
  const out: AccessChannel[] = [];
  for (const item of raw) {
    const ch = parseAccessChannel(item);
    if (ch && !out.includes(ch)) {
      out.push(ch);
    }
  }
  return out;
}

export function parseProductAudience(raw: unknown): ProductAudience | null {
  if (typeof raw !== 'string') {
    return null;
  }
  const code = raw.trim().toUpperCase();
  if (code === 'MARCHAND' || code === 'MERCHANTS') {
    return 'MERCHANT';
  }
  if (code === 'PARTICULIER' || code === 'PERSONAL' || code === 'B2C') {
    return 'RETAIL';
  }
  if (code === 'ENTREPRISE' || code === 'B2B' || code === 'BUSINESS') {
    return 'CORPORATE';
  }
  return PRODUCT_AUDIENCES.some((a) => a.code === code) ? (code as ProductAudience) : null;
}

export function parseProductAudiences(raw: unknown): ProductAudience[] {
  if (!Array.isArray(raw)) {
    return [];
  }
  const out: ProductAudience[] = [];
  for (const item of raw) {
    const a = parseProductAudience(item);
    if (a && !out.includes(a)) {
      out.push(a);
    }
  }
  return out;
}

/** Construit la méta journey/channel/audience depuis casting PUBLISHED ou config version. */
export function productJourneyMeta(
  productCode: string,
  source?: {
    journeyCode?: string | null;
    supportedChannels?: string[] | null;
    defaultChannel?: string | null;
    supportedAudiences?: string[] | null;
    defaultAudience?: string | null;
    configuration?: Record<string, unknown> | null;
  } | null,
): ProductJourneyMeta {
  const cfg = source?.configuration ?? null;
  const journeyCode =
    (typeof source?.journeyCode === 'string' && source.journeyCode.trim()
      ? source.journeyCode.trim()
      : null) ||
    (typeof cfg?.['journeyCode'] === 'string' && String(cfg['journeyCode']).trim()
      ? String(cfg['journeyCode']).trim()
      : null) ||
    defaultJourneyCode(productCode) ||
    null;

  const fromSource = parseAccessChannels(source?.supportedChannels);
  const fromCfg = parseAccessChannels(cfg?.['supportedChannels']);
  const supportedChannels = fromSource.length > 0 ? fromSource : fromCfg;

  const defaultChannel =
    parseAccessChannel(source?.defaultChannel) ||
    parseAccessChannel(cfg?.['defaultChannel']) ||
    parseAccessChannel(cfg?.['default_channel']) ||
    supportedChannels[0] ||
    null;

  const audiencesFromSource = parseProductAudiences(source?.supportedAudiences);
  const audiencesFromCfg = parseProductAudiences(
    cfg?.['supportedAudiences'] ?? cfg?.['supported_audiences'],
  );
  const supportedAudiences =
    audiencesFromSource.length > 0 ? audiencesFromSource : audiencesFromCfg;

  const defaultAudience =
    parseProductAudience(source?.defaultAudience) ||
    parseProductAudience(cfg?.['defaultAudience']) ||
    parseProductAudience(cfg?.['default_audience']) ||
    supportedAudiences[0] ||
    null;

  return { journeyCode, supportedChannels, defaultChannel, supportedAudiences, defaultAudience };
}

/** Produit avec sélection canal (config catalogue ou cash-out/in connus). */
export function productRequiresAccessChannel(
  productCode: string,
  meta?: ProductJourneyMeta | null,
): boolean {
  if (meta && meta.supportedChannels.length > 0) {
    return true;
  }
  const code = productCode.trim().toUpperCase();
  return code === 'CASH_OUT' || code === 'CASH_IN';
}

export function channelOptionsFor(meta: ProductJourneyMeta | null | undefined) {
  if (!meta?.supportedChannels?.length) {
    return [...ACCESS_CHANNELS];
  }
  return ACCESS_CHANNELS.filter((c) => meta.supportedChannels.includes(c.code));
}

export function audienceOptionsFor(meta: ProductJourneyMeta | null | undefined) {
  if (!meta?.supportedAudiences?.length) {
    return [...PRODUCT_AUDIENCES];
  }
  return PRODUCT_AUDIENCES.filter((a) => meta.supportedAudiences.includes(a.code));
}

export function productRequiresAudience(
  meta?: ProductJourneyMeta | null,
): boolean {
  return !!meta?.supportedAudiences?.length;
}

export function screenCatalogCode(screen: IntegrationScreen | null | undefined): string | null {
  const code = screen?.meta?.['catalogServiceCode'];
  return typeof code === 'string' && code.trim() ? code : null;
}

export function screenAccessChannel(screen: IntegrationScreen | null | undefined): AccessChannel | null {
  return parseAccessChannel(screen?.meta?.['accessChannel'] ?? screen?.meta?.['channel']);
}

export function screenJourneyCode(screen: IntegrationScreen | null | undefined): string | null {
  const code = screen?.meta?.['journeyCode'];
  return typeof code === 'string' && code.trim() ? code.trim() : null;
}

/** Body de démarrage minimal — le BPMN / formulaires collectent le reste. */
export function defaultProductStartBody(
  productCode: string,
  opts?: {
    channel?: AccessChannel;
    audience?: ProductAudience;
    journeyCode?: string;
    billerCode?: string;
    productCode?: string;
    billerProductCode?: string;
  },
): Record<string, unknown> {
  const code = productCode.trim().toUpperCase();
  const ref = `WEB-${Date.now()}`;
  const currency = 'XOF';
  const channel = opts?.channel;
  const audience = opts?.audience;
  const journeyCode = opts?.journeyCode ?? defaultJourneyCode(code);
  const billerCode = opts?.billerCode?.trim();
  const billerProductCode = (opts?.billerProductCode ?? opts?.productCode)?.trim();

  const withJourney = (body: Record<string, unknown>): Record<string, unknown> => {
    const out = { ...body };
    if (journeyCode) {
      out['journeyCode'] = journeyCode;
    }
    if (channel) {
      out['channel'] = channel;
      out['accessChannel'] = channel;
    }
    if (audience) {
      out['audience'] = audience;
      out['productAudience'] = audience;
    }
    if (billerCode) {
      out['billerCode'] = billerCode;
    }
    if (billerProductCode) {
      out['productCode'] = billerProductCode;
    }
    return out;
  };

  switch (code) {
    case 'ACCOUNT_TRANSFER':
      return withJourney({ amount: 100_000, currency, customerReference: ref });
    case 'RTGS':
      return withJourney({ amount: 1_000_000, currency, customerReference: ref, skipCutoffCheck: true });
    case 'SWIFT':
    case 'INTERNATIONAL_TRANSFER':
      return withJourney({ amount: 500_000, currency, customerReference: ref });
    case 'BANK_TO_WALLET':
    case 'WALLET_TO_BANK':
      return withJourney({ amount: 25_000, currency, customerReference: ref });
    case 'REMITTANCE':
      return withJourney({ amount: 50_000, currency, receiveCurrency: 'EUR', customerReference: ref });
    case 'REMITTANCE_RECEIVE':
      return withJourney({ customerReference: ref });
    case 'BILL_PAYMENT':
      return withJourney({ currency, customerReference: ref });
    case 'CASH_OUT':
    case 'CASH_IN':
      return withJourney({ amount: 25_000, currency, customerReference: ref });
    case 'LINK':
      return withJourney({ currency, customerReference: ref, phoneNumber: '22670000001' });
    case 'CHECKOUT':
    case 'PAYEMENT':
    case 'PAYMENT':
      return withJourney({ amount: 1_000, currency, customerReference: ref });
    default:
      return withJourney({ currency, customerReference: ref });
  }
}
