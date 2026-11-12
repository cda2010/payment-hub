/**
 * Contrat client portable payment-2 — rfDataSource / onChange.
 * Même algo sur mobile, payment-front, hubs RapidFrame, et tout autre client.
 *
 * A) Formio natif : customConditional, calculateValue, logic, redrawOn
 *    (si le client rend Form.io)
 * B) Cascade : dependsOn + paramsFrom → GET /api/v1/gateway/data-sources/{code}
 * C) Show allégé (non-Formio) : properties.rfShowWhen
 */

export type RfDataSourceConfig = {
  code: string;
  origin?: string;
  params?: Record<string, unknown>;
  dependsOn?: string | string[];
  paramsFrom?: Record<string, string>;
  productCode?: string;
};

export type RfShowWhen = {
  field: string;
  equals?: string;
  /** Visible si la valeur figure dans la liste (ex. wallets). */
  in?: string[];
  notEmpty?: boolean;
};

export function extractRfDataSource(
  comp: Record<string, unknown> | null | undefined
): RfDataSourceConfig | null {
  if (!comp) return null;
  const direct = comp['rfDataSource'];
  if (direct && typeof direct === 'object') {
    return normalizeRf(direct as Record<string, unknown>);
  }
  const props = comp['properties'];
  if (props && typeof props === 'object') {
    const rf = (props as Record<string, unknown>)['rfDataSource'];
    if (rf && typeof rf === 'object') {
      return normalizeRf(rf as Record<string, unknown>);
    }
  }
  return null;
}

/**
 * Montants exemples (chips) — `properties.amountChips` sur le champ montant.
 * Accepte un tableau de nombres ou une chaîne `"1000,2000,5000"`.
 */
export function extractAmountChips(
  comp: Record<string, unknown> | null | undefined
): number[] | null {
  if (!comp) return null;
  const direct = comp['amountChips'];
  const fromDirect = normalizeAmountChips(direct);
  if (fromDirect?.length) return fromDirect;
  const props = comp['properties'];
  if (props && typeof props === 'object') {
    return normalizeAmountChips((props as Record<string, unknown>)['amountChips']);
  }
  return null;
}

function normalizeAmountChips(raw: unknown): number[] | null {
  if (raw == null) return null;
  if (Array.isArray(raw)) {
    const nums = raw
      .map((v) => (typeof v === 'number' ? v : Number(String(v).replace(/\s/g, '').replace(',', '.'))))
      .filter((n) => Number.isFinite(n) && n > 0);
    return nums.length ? nums : null;
  }
  if (typeof raw === 'string' && raw.trim()) {
    const nums = raw
      .split(/[,;\s]+/)
      .map((s) => Number(s.trim().replace(',', '.')))
      .filter((n) => Number.isFinite(n) && n > 0);
    return nums.length ? nums : null;
  }
  return null;
}

export function extractRfShowWhen(
  comp: Record<string, unknown> | null | undefined
): RfShowWhen | null {
  const props = comp?.['properties'];
  if (!props || typeof props !== 'object') return null;
  const sw = (props as Record<string, unknown>)['rfShowWhen'];
  if (!sw || typeof sw !== 'object') return null;
  const field = (sw as Record<string, unknown>)['field'];
  if (field == null || String(field).trim() === '') return null;
  const inRaw = (sw as Record<string, unknown>)['in'];
  const inList = Array.isArray(inRaw)
    ? inRaw.map((v) => String(v).trim()).filter(Boolean)
    : undefined;
  return {
    field: String(field).trim(),
    equals:
      (sw as Record<string, unknown>)['equals'] != null
        ? String((sw as Record<string, unknown>)['equals'])
        : undefined,
    in: inList?.length ? inList : undefined,
    notEmpty: (sw as Record<string, unknown>)['notEmpty'] === true
  };
}

function normalizeRf(raw: Record<string, unknown>): RfDataSourceConfig | null {
  const code = raw['code'] != null ? String(raw['code']).trim() : '';
  if (!code) return null;
  let dependsOn: string[] | undefined;
  const dep = raw['dependsOn'];
  if (Array.isArray(dep)) {
    dependsOn = dep.map((d) => String(d).trim()).filter(Boolean);
  } else if (typeof dep === 'string' && dep.trim()) {
    dependsOn = dep.split(',').map((s) => s.trim()).filter(Boolean);
  }
  const paramsFrom =
    raw['paramsFrom'] && typeof raw['paramsFrom'] === 'object'
      ? Object.fromEntries(
          Object.entries(raw['paramsFrom'] as Record<string, unknown>).map(([k, v]) => [
            k,
            String(v)
          ])
        )
      : undefined;
  const params =
    raw['params'] && typeof raw['params'] === 'object'
      ? { ...(raw['params'] as Record<string, unknown>) }
      : undefined;
  return {
    code,
    origin: raw['origin'] != null ? String(raw['origin']) : 'LOCAL',
    params,
    dependsOn,
    paramsFrom,
    productCode: raw['productCode'] != null ? String(raw['productCode']) : undefined
  };
}

export function dependsOnList(rf: RfDataSourceConfig): string[] {
  if (!rf.dependsOn) return [];
  return Array.isArray(rf.dependsOn) ? rf.dependsOn : [rf.dependsOn];
}

export function buildDataSourceQuery(
  rf: RfDataSourceConfig,
  formData: Record<string, unknown>
): Record<string, string> {
  const out: Record<string, string> = {
    origin: rf.origin?.trim() || 'LOCAL'
  };
  if (rf.productCode?.trim()) out['productCode'] = rf.productCode.trim();
  if (rf.params) {
    for (const [k, v] of Object.entries(rf.params)) {
      if (v != null && String(v).trim() !== '') out[k] = String(v).trim();
    }
  }
  if (rf.paramsFrom) {
    for (const [param, fieldKey] of Object.entries(rf.paramsFrom)) {
      const val = formData[fieldKey];
      if (val != null && String(val).trim() !== '') out[param] = String(val).trim();
    }
  }
  return out;
}

export function parentsReady(rf: RfDataSourceConfig, formData: Record<string, unknown>): boolean {
  const deps = dependsOnList(rf);
  if (!deps.length) return true;
  return deps.every((k) => {
    const v = formData[k];
    return v != null && String(v).trim() !== '';
  });
}

export function isShowWhenVisible(
  showWhen: RfShowWhen | null | undefined,
  formData: Record<string, unknown>
): boolean {
  if (!showWhen?.field) return true;
  const v = formData[showWhen.field];
  if (showWhen.notEmpty) return v != null && String(v).trim() !== '';
  if (showWhen.equals !== undefined) return String(v ?? '') === String(showWhen.equals);
  if (showWhen.in?.length) {
    const cur = String(v ?? '').trim();
    if (!cur) return false;
    return showWhen.in.some((x) => x === cur);
  }
  return true;
}
