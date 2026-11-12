import { FormField, FormioSchemaDto } from '../../core/model/api.models';

export type FormioSchema = {
  display: string;
  components: Record<string, unknown>[];
};

/**
 * Builds a Form.io schema from payment-2 FormField descriptors
 * (legacy configurationJson → nextAction.fields).
 */
export function buildFormioFromPaymentFields(
  fields: FormField[] | null | undefined,
): FormioSchema {
  const components = (fields ?? []).map((field) => toFormioComponent(field));
  ensureHiddenSubmit(components);
  return { display: 'form', components };
}

/** Prefer API formSchema; fall back to building from fields. */
export function resolveFormioSchema(
  formSchema: FormioSchemaDto | null | undefined,
  fields: FormField[] | null | undefined,
  isAlreadyCollected: (name: string) => boolean,
): FormioSchema {
  if (formSchema?.components && Array.isArray(formSchema.components)) {
    const components = formSchema.components
      .filter((comp) => {
        const key = String(comp['key'] ?? '');
        if (!key || key === 'submit') {
          return true;
        }
        return !isAlreadyCollected(key);
      })
      .map((c) => ({ ...c }));
    ensureHiddenSubmit(components);
    return {
      display: String(formSchema.display ?? 'form'),
      components,
    };
  }
  const visible = (fields ?? []).filter((f) => !isAlreadyCollected(f.name));
  return buildFormioFromPaymentFields(visible);
}

/** Parse step configurationJson → Form.io schema for the admin builder. */
export function parseConfigurationJsonToFormio(configurationJson: string | null | undefined): FormioSchema {
  if (!configurationJson || !configurationJson.trim()) {
    return emptyFormioSchema();
  }
  try {
    const cfg = JSON.parse(configurationJson) as {
      formio?: FormioSchemaDto;
      fields?: FormField[];
    };
    if (cfg.formio?.components && Array.isArray(cfg.formio.components)) {
      const components = cfg.formio.components.map((c) => ({ ...c }));
      ensureHiddenSubmit(components);
      return {
        display: String(cfg.formio.display ?? 'form'),
        components,
      };
    }
    if (Array.isArray(cfg.fields) && cfg.fields.length) {
      return buildFormioFromPaymentFields(cfg.fields);
    }
  } catch {
    // fall through
  }
  return emptyFormioSchema();
}

/** Serialize Form.io schema as configurationJson Phase 2. */
export function serializeFormioConfiguration(schema: FormioSchema | FormioSchemaDto | null | undefined): string {
  const components = [...(schema?.components ?? [])].map((c) => ({ ...c }));
  ensureHiddenSubmit(components);
  return JSON.stringify({
    formio: {
      display: String(schema?.display ?? 'form'),
      components,
    },
  });
}

export function emptyFormioSchema(): FormioSchema {
  const components: Record<string, unknown>[] = [];
  ensureHiddenSubmit(components);
  return { display: 'form', components };
}

function ensureHiddenSubmit(components: Record<string, unknown>[]): void {
  if (components.some((c) => c['key'] === 'submit')) {
    return;
  }
  components.push({
    type: 'button',
    key: 'submit',
    label: 'Submit',
    input: true,
    tableView: false,
    disableOnInvalid: false,
    hidden: true,
  });
}

function toFormioComponent(field: FormField): Record<string, unknown> {
  const type = (field.type || 'text').toLowerCase();
  const validate: Record<string, unknown> = {
    required: !!field.required,
  };
  if (field.pattern) {
    validate['pattern'] = field.pattern;
  }

  const base: Record<string, unknown> = {
    key: field.name,
    label: field.label || field.name,
    input: true,
    tableView: true,
    validate,
    placeholder: field.placeholder ?? undefined,
  };

  switch (type) {
    case 'tel':
    case 'phone':
      return {
        ...base,
        type: 'phoneNumber',
      };
    case 'number':
    case 'money':
    case 'amount':
      return {
        ...base,
        type: 'number',
        delimiter: true,
        requireDecimal: false,
      };
    case 'email':
      return {
        ...base,
        type: 'email',
      };
    case 'password':
      return {
        ...base,
        type: 'password',
      };
    case 'textarea':
      return {
        ...base,
        type: 'textarea',
        rows: 3,
      };
    case 'select':
      return {
        ...base,
        type: 'select',
        dataSrc: 'values',
        data: { values: [] },
        widget: 'choicesjs',
      };
    case 'text':
    default:
      return {
        ...base,
        type: 'textfield',
      };
  }
}

/** Flat map for SUBMIT_FORM — strips Form.io internal keys. */
export function formioDataToPaymentData(
  formData: Record<string, unknown> | undefined,
): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(formData ?? {})) {
    if (k === 'submit' || k.startsWith('metadata') || k === 'state') {
      continue;
    }
    out[k] = v;
  }
  return out;
}
