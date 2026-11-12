import {
  extractAmountChips,
  extractRfDataSource,
  extractRfShowWhen
} from './rf-datasource';
import type { GatewayField, GatewayProductField, IntegrationScreen } from './types';

/** Mappe un champ produit / écran gateway vers {@link GatewayField}. */
export function mapGatewayField(raw: GatewayProductField | Record<string, unknown>): GatewayField {
  const r = raw as Record<string, unknown>;
  const name = String(r['name'] ?? r['code'] ?? r['id'] ?? r['key'] ?? '').trim();
  const label = String(r['label'] ?? name);
  const type = String(r['type'] ?? 'TEXT').toUpperCase();
  const options = extractOptions(r['options'] ?? r['values'] ?? r['data']);
  const min = numOrUndef(r['min'] ?? r['minimum']);
  const max = numOrUndef(r['max'] ?? r['maximum']);
  return {
    code: name || 'field',
    label,
    type: fieldType(name, type),
    required: isRequired(r),
    placeholder: r['placeholder'] != null ? String(r['placeholder']) : undefined,
    pattern: r['pattern'] != null ? String(r['pattern']) : undefined,
    options,
    min,
    max
  };
}

export function mapGatewayFields(
  fields?: Array<GatewayProductField | Record<string, unknown>> | null
): GatewayField[] {
  return (fields || [])
    .map((f) => mapGatewayField(f))
    .filter((f) => !!f.code);
}

/**
 * Champs à afficher pour un IntegrationScreen FORM.
 * Préfère Form.io `components[]`, enrichit `fields[]` plats.
 */
export function fieldsFromIntegrationScreen(
  screen: IntegrationScreen | null | undefined
): GatewayField[] {
  if (!screen) return [];
  const skip = schemaComponents(screen.formSchema).nonInput;
  const visible = (f: GatewayField): boolean => !skip.has(f.code);
  const fromSchema = fieldsFromFormioComponents(screen.formSchema).filter(visible);
  const fromFields = mapGatewayFields(screen.fields).filter(visible);

  if (fromSchema.length && fromFields.length) {
    return mergeFieldLists(fromFields, fromSchema);
  }
  if (fromSchema.length) return fromSchema;
  if (fromFields.length) return fromFields;

  const schema = screen.formSchema;
  if (schema && typeof schema === 'object') {
    const props = (schema as { properties?: Record<string, Record<string, unknown>> }).properties;
    if (props && typeof props === 'object') {
      return Object.entries(props).map(([code, def]) =>
        mapGatewayField({
          name: code,
          label: def['title'] ?? def['label'] ?? code,
          type: String(def['type'] ?? 'TEXT'),
          required: Array.isArray((schema as { required?: string[] }).required)
            ? (schema as { required: string[] }).required.includes(code)
            : false
        })
      );
    }
  }
  return [];
}

export function fieldsFromProductCollect(
  lookupFields?: GatewayProductField[] | null,
  amountFields?: GatewayProductField[] | null
): GatewayField[] {
  return [...mapGatewayFields(lookupFields), ...mapGatewayFields(amountFields)];
}

export function productNeedsLocalCollect(product: {
  journeyCode?: string | null;
  amountFields?: GatewayProductField[] | null;
  amountPolicy?: string | null;
}): boolean {
  const journey = String(product.journeyCode || '').toUpperCase();
  const amounts = product.amountFields || [];
  if (amounts.length > 0) return true;
  if (journey === 'PREPAID') return true;
  const policy = String(product.amountPolicy || '').toUpperCase();
  return policy === 'EDITABLE';
}

export function fieldsFromFormioComponents(
  formSchema: Record<string, unknown> | null | undefined
): GatewayField[] {
  if (!formSchema || typeof formSchema !== 'object') return [];
  const components = formSchema['components'];
  if (!Array.isArray(components)) return [];
  const out: GatewayField[] = [];
  walkFormioComponents(components, out);
  return out;
}

export function hiddenDefaultsFromIntegrationScreen(
  screen: IntegrationScreen | null | undefined
): Record<string, unknown> {
  return schemaComponents(screen?.formSchema).defaults;
}

export function displayHtmlFromIntegrationScreen(
  screen: IntegrationScreen | null | undefined
): string {
  return schemaComponents(screen?.formSchema).html.join('');
}

/** FORM sans champ / message / HTML récap → pass-through auto SUBMIT_FORM. */
export function isPassThroughForm(screen: IntegrationScreen): boolean {
  return (
    fieldsFromIntegrationScreen(screen).length === 0 &&
    !String(screen.message || '').trim() &&
    !displayHtmlFromIntegrationScreen(screen).trim()
  );
}

/**
 * Un montant encore à 0 côté backend ne doit pas pré-remplir le champ montant.
 */
export function withoutPendingAmount(data: Record<string, unknown>): Record<string, unknown> {
  if (!('amount' in data) || Number(data['amount']) > 0) {
    return data;
  }
  const { amount: _amount, ...rest } = data;
  return rest;
}

export function productJourneyHint(
  journeyCode?: string | null,
  lookupMode?: string | null
): string {
  const j = (journeyCode || '').toUpperCase();
  if (j === 'PREPAID') return 'Prépayé';
  if (j === 'OPEN_BILLS') return 'Factures ouvertes';
  const l = (lookupMode || '').toUpperCase();
  if (l === 'BILL_REF') return 'Réf. facture';
  if (l === 'SUBSCRIBER') return 'Réf. client';
  if (l === 'BOTH') return 'Client ou facture';
  return journeyCode || lookupMode || 'Produit';
}

function walkFormioComponents(components: unknown[], out: GatewayField[]): void {
  for (const raw of components) {
    if (!raw || typeof raw !== 'object') continue;
    const c = raw as Record<string, unknown>;
    const type = String(c['type'] || '').toLowerCase();
    const key = c['key'] != null ? String(c['key']).trim() : '';

    if (type === 'button' || type === 'htmlelement' || type === 'content') continue;
    if (isHiddenComponent(c)) continue;

    if (
      type === 'panel' ||
      type === 'fieldset' ||
      type === 'well' ||
      type === 'tabs' ||
      type === 'table'
    ) {
      if (Array.isArray(c['components'])) {
        walkFormioComponents(c['components'] as unknown[], out);
      }
      continue;
    }

    if (type === 'columns' && Array.isArray(c['columns'])) {
      for (const col of c['columns'] as Array<Record<string, unknown>>) {
        if (Array.isArray(col['components'])) {
          walkFormioComponents(col['components'] as unknown[], out);
        }
      }
      continue;
    }

    if (!key || key === 'submit') continue;

    const validate = (c['validate'] as Record<string, unknown> | undefined) || {};
    const data = (c['data'] as Record<string, unknown> | undefined) || {};
    const field = mapGatewayField({
      name: key,
      label: c['label'] ?? key,
      type: formioTypeToGateway(type),
      required: isTruthy(validate['required']) || isTruthy(c['required']),
      placeholder: c['placeholder'],
      pattern: validate['pattern'],
      options: extractOptions(c['values'] ?? c['data'] ?? data['values']),
      min: validate['min'] ?? c['min'],
      max: validate['max'] ?? c['max'],
      defaultValue: c['defaultValue']
    });
    field.rfDataSource = extractRfDataSource(c);
    field.showWhen = extractRfShowWhen(c);
    field.amountChips = extractAmountChips(c);
    out.push(field);
  }
}

interface SchemaScan {
  nonInput: Set<string>;
  defaults: Record<string, unknown>;
  html: string[];
}

function schemaComponents(formSchema: Record<string, unknown> | null | undefined): SchemaScan {
  const acc: SchemaScan = { nonInput: new Set<string>(), defaults: {}, html: [] };
  const components = formSchema?.['components'];
  if (Array.isArray(components)) {
    scanComponents(components, acc);
  }
  return acc;
}

function scanComponents(components: unknown[], acc: SchemaScan): void {
  for (const raw of components) {
    if (!raw || typeof raw !== 'object') continue;
    const c = raw as Record<string, unknown>;

    if (Array.isArray(c['components'])) {
      scanComponents(c['components'] as unknown[], acc);
    }
    if (Array.isArray(c['columns'])) {
      for (const col of c['columns'] as Array<Record<string, unknown>>) {
        if (Array.isArray(col['components'])) {
          scanComponents(col['components'] as unknown[], acc);
        }
      }
    }

    const type = String(c['type'] || '').toLowerCase();
    const key = c['key'] != null ? String(c['key']).trim() : '';
    if (!key || key === 'submit') continue;

    if (type === 'htmlelement' || type === 'content') {
      acc.nonInput.add(key);
      const content = c['content'] ?? c['html'];
      if (typeof content === 'string' && content.trim()) {
        acc.html.push(content);
      }
      continue;
    }
    if (isHiddenComponent(c)) {
      acc.nonInput.add(key);
      if (c['defaultValue'] != null) acc.defaults[key] = c['defaultValue'];
    }
  }
}

function isHiddenComponent(c: Record<string, unknown>): boolean {
  return String(c['type'] || '').toLowerCase() === 'hidden' || isTruthy(c['hidden']);
}

function formioTypeToGateway(type: string): string {
  switch (type) {
    case 'number':
      return 'NUMBER';
    case 'currency':
      return 'MONEY';
    case 'phoneNumber':
      return 'PHONE';
    case 'email':
      return 'EMAIL';
    case 'datetime':
    case 'day':
      return 'DATE';
    case 'select':
    case 'radio':
    case 'selectboxes':
      return 'SELECT';
    case 'checkbox':
      return 'CHECKBOX';
    case 'textarea':
    case 'textfield':
    case 'password':
    default:
      return 'TEXT';
  }
}

function mergeFieldLists(base: GatewayField[], rich: GatewayField[]): GatewayField[] {
  const byCode = new Map(rich.map((f) => [f.code, f]));
  const merged = base.map((f) => {
    const r = byCode.get(f.code);
    if (!r) return f;
    return {
      ...f,
      type: r.type !== 'TEXT' || f.type === 'TEXT' ? r.type : f.type,
      options: f.options?.length ? f.options : r.options,
      label: f.label || r.label,
      required: f.required || r.required,
      min: f.min ?? r.min,
      max: f.max ?? r.max,
      placeholder: f.placeholder ?? r.placeholder,
      pattern: f.pattern ?? r.pattern,
      rfDataSource: f.rfDataSource ?? r.rfDataSource,
      showWhen: f.showWhen ?? r.showWhen,
      amountChips: f.amountChips?.length ? f.amountChips : r.amountChips
    };
  });
  const seen = new Set(merged.map((f) => f.code));
  for (const r of rich) {
    if (!seen.has(r.code)) merged.push(r);
  }
  return merged;
}

function extractOptions(raw: unknown): GatewayField['options'] {
  if (!raw) return undefined;
  if (Array.isArray(raw)) {
    const opts = raw
      .map((o) => {
        if (typeof o === 'string' || typeof o === 'number') {
          return { label: String(o), value: String(o) };
        }
        if (o && typeof o === 'object') {
          const m = o as Record<string, unknown>;
          const value = m['value'] ?? m['id'] ?? m['code'] ?? m['label'];
          const label = m['label'] ?? m['name'] ?? value;
          if (value == null) return null;
          const opt: { label: string; value: string; icon?: string; sub?: string } = {
            label: String(label),
            value: String(value)
          };
          if (m['icon'] != null && String(m['icon']).trim()) {
            opt.icon = String(m['icon']);
          }
          if (m['sub'] != null && String(m['sub']).trim()) {
            opt.sub = String(m['sub']);
          }
          return opt;
        }
        return null;
      })
      .filter((o): o is { label: string; value: string; icon?: string; sub?: string } => !!o);
    return opts.length ? opts : undefined;
  }
  if (typeof raw === 'object') {
    const data = raw as Record<string, unknown>;
    if (Array.isArray(data['values'])) return extractOptions(data['values']);
  }
  return undefined;
}

function fieldType(name: string, rawType: string): GatewayField['type'] {
  const type = normalizeFieldType(rawType);
  return type === 'NUMBER' && name.toLowerCase() === 'amount' ? 'MONEY' : type;
}

function normalizeFieldType(type: string): GatewayField['type'] {
  switch (type.toUpperCase()) {
    case 'MONEY':
    case 'AMOUNT':
    case 'CURRENCY':
      return 'MONEY';
    case 'NUMBER':
    case 'INTEGER':
    case 'DECIMAL':
      return 'NUMBER';
    case 'PHONE':
    case 'TEL':
    case 'MSISDN':
      return 'PHONE';
    case 'EMAIL':
      return 'EMAIL';
    case 'DATE':
    case 'DATETIME':
      return 'DATE';
    case 'SELECT':
    case 'ENUM':
    case 'RADIO':
      return 'SELECT';
    case 'CHECKBOX':
    case 'BOOLEAN':
    case 'BOOL':
      return 'CHECKBOX';
    default:
      return 'TEXT';
  }
}

function isRequired(r: Record<string, unknown>): boolean {
  if (isTruthy(r['required'])) return true;
  const validate = r['validate'];
  if (validate && typeof validate === 'object') {
    return isTruthy((validate as Record<string, unknown>)['required']);
  }
  return false;
}

function isTruthy(v: unknown): boolean {
  return v === true || v === 'true' || v === 1 || v === '1';
}

function numOrUndef(v: unknown): number | undefined {
  if (v == null || v === '') return undefined;
  const n = Number(v);
  return Number.isFinite(n) ? n : undefined;
}
