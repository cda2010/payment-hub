import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  inject,
  output,
} from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Subscription, debounceTime, merge } from 'rxjs';
import { map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageModule } from 'primeng/message';
import { FormField, FormioSchemaDto } from '../../core/model/api.models';
import { GatewayService } from '../../core/service/gateway.service';
import type { GatewayField } from '@payment2/gateway-core';
import { fieldsFromFormioComponents, isShowWhenVisible } from '@payment2/gateway-core';
import {
  RfDataSourceConfig,
  buildDataSourceQuery,
  dependsOnList,
  parentsReady,
} from '../../shared/formio/rf-datasource.util';

export interface GatewayFormField {
  name: string;
  label: string;
  type: 'text' | 'number' | 'tel' | 'email' | 'password' | 'textarea' | 'select' | 'checkbox';
  required: boolean;
  placeholder?: string;
  options?: Array<{ label: string; value: string }>;
  disabled?: boolean;
  /** Cascade data-source (dependsOn). */
  rfDataSource?: RfDataSourceConfig | null;
  /**
   * Visibilité déclarative (équivalent allégé de customConditional Formio).
   * properties.rfShowWhen: { field, equals } | { field, notEmpty: true }
   */
  showWhen?: { field: string; equals?: string; in?: string[]; notEmpty?: boolean } | null;
  /** Montants exemples (Form.io `properties.amountChips`). */
  amountChips?: number[] | null;
}

/**
 * Formulaire parcours gateway en contrôles PrimeNG
 * (remplace Form.io / RF pour l'UI produit).
 *
 * Gère :
 * - A) rfShowWhen (show/hide type Formio conditional)
 * - B) rfDataSource.dependsOn → re-fetch options via GET /data-sources/{code}
 */
@Component({
  selector: 'app-gateway-primeng-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    CheckboxModule,
    DropdownModule,
    InputNumberModule,
    InputTextModule,
    InputTextareaModule,
    MessageModule,
  ],
  template: `
    @if (message) {
      <p-message severity="info" [text]="message" styleClass="w-full mb" />
    }

    @if (!visibleFields.length) {
      <p-message
        severity="warn"
        text="Aucun champ à saisir — vous pouvez continuer."
        styleClass="w-full mb"
      />
    }

    <form [formGroup]="form" (ngSubmit)="submit()" class="gw-form">
      @for (field of visibleFields; track field.name) {
        <div class="field">
          @if (field.type !== 'checkbox') {
            <label [attr.for]="field.name">
              {{ field.label }}
              @if (field.required) {
                <span class="req">*</span>
              }
            </label>
          }

          @switch (field.type) {
            @case ('select') {
              @if (field.name === 'billReference' && (field.options?.length ?? 0) > 0) {
                <div class="bill-list" role="listbox" [attr.aria-label]="field.label">
                  @for (opt of field.options; track opt.value) {
                    <button
                      type="button"
                      class="bill-card"
                      role="option"
                      [class.selected]="form.get(field.name)?.value === opt.value"
                      [attr.aria-selected]="form.get(field.name)?.value === opt.value"
                      (click)="selectBillOption(field.name, opt.value)"
                    >
                      <div class="bill-card-main">{{ opt.label }}</div>
                      <div class="bill-card-ref">{{ opt.value }}</div>
                    </button>
                  }
                </div>
              } @else {
                <p-dropdown
                  [inputId]="field.name"
                  [formControlName]="field.name"
                  [options]="field.options || []"
                  optionLabel="label"
                  optionValue="value"
                  [placeholder]="field.placeholder || 'Sélectionner'"
                  styleClass="w-full"
                  [disabled]="!!field.disabled"
                />
              }
            }
            @case ('number') {
              <p-inputNumber
                [inputId]="field.name"
                [formControlName]="field.name"
                styleClass="w-full"
                inputStyleClass="w-full"
                [disabled]="!!field.disabled"
                [minFractionDigits]="0"
                [maxFractionDigits]="0"
                locale="fr-FR"
              />
              @if (field.amountChips?.length) {
                <div class="amount-chips" role="listbox" [attr.aria-label]="'Montants suggérés'">
                  @for (a of field.amountChips; track a) {
                    <button
                      type="button"
                      class="amt-chip"
                      role="option"
                      [class.on]="form.get(field.name)?.value === a"
                      [attr.aria-selected]="form.get(field.name)?.value === a"
                      (click)="pickAmount(field.name, a)"
                    >
                      <span class="amt-value">{{ formatChip(a) }}</span>
                      <span class="amt-unit">F</span>
                    </button>
                  }
                </div>
              }
            }
            @case ('textarea') {
              <textarea
                pInputTextarea
                [id]="field.name"
                [formControlName]="field.name"
                rows="3"
                class="w-full"
                [placeholder]="field.placeholder || ''"
                [attr.disabled]="field.disabled ? true : null"
              ></textarea>
            }
            @case ('checkbox') {
              <div class="check-row">
                <p-checkbox
                  [inputId]="field.name"
                  [formControlName]="field.name"
                  [binary]="true"
                  [disabled]="!!field.disabled"
                />
                <label [attr.for]="field.name">{{ field.label }}</label>
              </div>
            }
            @default {
              <input
                pInputText
                [id]="field.name"
                [formControlName]="field.name"
                [type]="field.type"
                class="w-full"
                [placeholder]="field.placeholder || ''"
                [attr.disabled]="field.disabled ? true : null"
              />
            }
          }

          @if (form.get(field.name)?.invalid && form.get(field.name)?.touched) {
            <small class="err">Champ requis</small>
          }
        </div>
      }

      <button
        pButton
        type="submit"
        icon="pi pi-arrow-right"
        iconPos="right"
        label="Continuer"
        [disabled]="form.invalid || submitting"
        [loading]="submitting"
      ></button>
    </form>
  `,
  styles: `
    .gw-form { display: flex; flex-direction: column; gap: 1rem; }
    .field label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; font-weight: 500; }
    .req { color: var(--red-500, #ef4444); }
    .check-row { display: flex; align-items: center; gap: 0.5rem; }
    .check-row label { margin: 0; }
    .err { color: var(--red-500, #ef4444); font-size: 0.8rem; }
    .bill-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .bill-card {
      text-align: left;
      border: 1px solid var(--surface-border, #e2e8f0);
      border-radius: 10px;
      padding: 0.85rem 1rem;
      background: var(--surface-ground, #f8fafc);
      cursor: pointer;
    }
    .bill-card.selected {
      border-color: var(--primary-color, #2563eb);
      box-shadow: 0 0 0 1px var(--primary-color, #2563eb);
      background: #fff;
    }
    .bill-card-main { font-weight: 600; font-size: 0.95rem; }
    .bill-card-ref { font-family: ui-monospace, monospace; font-size: 0.75rem; color: #64748b; margin-top: 0.25rem; }
    :host ::ng-deep .w-full { width: 100%; }
    :host ::ng-deep .mb { margin-bottom: 0.75rem; display: block; }
    :host ::ng-deep .p-inputnumber { width: 100%; }
    .amount-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 0.45rem;
      margin-top: 0.55rem;
    }
    .amt-chip {
      display: inline-flex;
      align-items: baseline;
      gap: 0.2rem;
      min-height: 2.25rem;
      padding: 0.4rem 0.85rem;
      border-radius: 10px;
      border: 1.5px solid var(--surface-border, #e2e8f0);
      background: var(--surface-card, #fff);
      color: var(--text-color, #0f172a);
      font: inherit;
      cursor: pointer;
      transition: background 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
    }
    .amt-chip:hover {
      border-color: var(--primary-color, #2563eb);
    }
    .amt-chip.on {
      border-color: var(--primary-color, #2563eb);
      background: color-mix(in srgb, var(--primary-color, #2563eb) 10%, #fff);
      color: var(--primary-color, #2563eb);
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary-color, #2563eb) 16%, transparent);
    }
    .amt-value {
      font-size: 0.88rem;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
    }
    .amt-unit {
      font-size: 0.7rem;
      font-weight: 600;
      opacity: 0.75;
    }
  `,
})
export class GatewayPrimengFormComponent implements OnChanges, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly gateway = inject(GatewayService);

  @Input() apiFields: FormField[] = [];
  @Input() apiFormSchema: FormioSchemaDto | null = null;
  @Input() initialData: Record<string, unknown> | null = null;
  @Input() message: string | null = null;
  @Input() submitting = false;
  /** Produit courant — transmis aux data-sources (ex. CATALOG_PARTICIPANTS). */
  @Input() productCode: string | null = null;

  readonly submitted = output<Record<string, unknown>>();

  fields: GatewayFormField[] = [];
  visibleFields: GatewayFormField[] = [];
  form: FormGroup = this.fb.group({});

  private formSub?: Subscription;
  private fetchSubs: Subscription[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['apiFields'] ||
      changes['apiFormSchema'] ||
      changes['initialData']
    ) {
      this.rebuild();
    }
  }

  ngOnDestroy(): void {
    this.formSub?.unsubscribe();
    this.fetchSubs.forEach((s) => s.unsubscribe());
  }

  pickAmount(fieldName: string, value: number): void {
    this.form.get(fieldName)?.setValue(value);
    this.form.get(fieldName)?.markAsDirty();
    this.form.get(fieldName)?.markAsTouched();
  }

  formatChip(n: number): string {
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n);
  }

  selectBillOption(fieldName: string, value: string): void {
    this.form.get(fieldName)?.setValue(value);
    this.form.get(fieldName)?.markAsTouched();
    const bills = this.initialData?.['bills'];
    if (!Array.isArray(bills)) {
      return;
    }
    const match = bills.find((b) => {
      if (!b || typeof b !== 'object') {
        return false;
      }
      const row = b as Record<string, unknown>;
      const ref = row['billReference'] ?? row['reference'] ?? row['id'];
      return String(ref ?? '') === value;
    }) as Record<string, unknown> | undefined;
    if (!match) {
      return;
    }
    const amount = match['amountDue'] ?? match['amount'];
    if (amount != null && this.form.get('amount')) {
      const n = typeof amount === 'number' ? amount : Number(String(amount).replace(',', '.'));
      if (!Number.isNaN(n)) {
        this.form.get('amount')?.setValue(n);
      }
    }
    const label = match['billLabel'] ?? match['label'] ?? match['customerName'];
    if (label != null && this.form.get('billLabel')) {
      this.form.get('billLabel')?.setValue(String(label));
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.submitting) {
      return;
    }
    this.submitted.emit({ ...this.form.getRawValue() });
  }

  private rebuild(): void {
    this.formSub?.unsubscribe();
    this.fetchSubs.forEach((s) => s.unsubscribe());
    this.fetchSubs = [];

    this.fields = resolveGatewayFields(this.apiFormSchema, this.apiFields);
    const group: Record<string, FormControl> = {};
    const data = this.initialData ?? {};
    for (const field of this.fields) {
      const validators = field.required ? [Validators.required] : [];
      let value: unknown = data[field.name];
      if (value === undefined || value === null) {
        value = field.type === 'checkbox' ? false : field.type === 'number' ? null : '';
      }
      group[field.name] = new FormControl(
        { value, disabled: !!field.disabled },
        validators,
      );
    }
    this.form = this.fb.group(group);
    this.refreshVisibility();
    this.wireReactions();
    this.refreshAllDependentSources();
  }

  private wireReactions(): void {
    const controls = Object.keys(this.form.controls).map((name) =>
      this.form.get(name)!.valueChanges.pipe(map((v) => ({ name, value: v }))),
    );
    if (!controls.length) {
      return;
    }
    this.formSub = merge(...controls)
      .pipe(debounceTime(120))
      .subscribe((changed) => {
        this.refreshVisibility();
        this.refreshDependentsOf(changed.name);
      });
  }

  private refreshVisibility(): void {
    const data = this.form.getRawValue() as Record<string, unknown>;
    this.visibleFields = this.fields.filter((f) => isFieldVisible(f, data));
  }

  private refreshAllDependentSources(): void {
    for (const field of this.fields) {
      if (field.rfDataSource && dependsOnList(field.rfDataSource).length) {
        this.fetchOptionsFor(field);
      }
    }
  }

  private refreshDependentsOf(changedField: string): void {
    for (const field of this.fields) {
      const deps = field.rfDataSource ? dependsOnList(field.rfDataSource) : [];
      if (deps.includes(changedField)) {
        this.form.get(field.name)?.setValue('', { emitEvent: false });
        this.fetchOptionsFor(field);
      }
    }
  }

  private fetchOptionsFor(field: GatewayFormField): void {
    const rf = field.rfDataSource;
    if (!rf) {
      return;
    }
    const data = this.form.getRawValue() as Record<string, unknown>;
    if (!parentsReady(rf, data)) {
      field.options = [];
      return;
    }
    const query = buildDataSourceQuery(rf, data);
    if (this.productCode?.trim() && !query['productCode']) {
      query['productCode'] = this.productCode.trim();
    }
    const sub = this.gateway.resolveDataSource(rf.code, query).subscribe({
      next: (resp) => {
        field.options = (resp.items ?? []).map((it) => ({
          label: it.label || it.id,
          value: String(it.value ?? it.id),
        }));
      },
      error: () => {
        field.options = [];
      },
    });
    this.fetchSubs.push(sub);
  }
}

function isFieldVisible(
  field: GatewayFormField,
  data: Record<string, unknown>,
): boolean {
  return isShowWhenVisible(field.showWhen, data);
}

/** Prefers Form.io schema components when present, else API fields. */
export function resolveGatewayFields(
  formSchema: FormioSchemaDto | null | undefined,
  fields: FormField[] | null | undefined,
): GatewayFormField[] {
  const schema = formSchema as Record<string, unknown> | null | undefined;
  const fromSchema = fieldsFromFormioComponents(schema).map(toPrimengField);
  if (fromSchema.length) {
    return fromSchema;
  }
  return (fields ?? [])
    .filter((f) => !!f?.name)
    .map((f) => ({
      name: f.name,
      label: f.label || f.name,
      type: mapSimpleType(f.type),
      required: !!f.required,
      placeholder: f.placeholder || undefined,
    }));
}

function toPrimengField(f: GatewayField): GatewayFormField {
  return {
    name: f.code,
    label: f.label,
    type: mapGatewayFieldType(f.type),
    required: !!f.required,
    placeholder: f.placeholder,
    options: f.options?.map((o) => ({ label: o.label, value: o.value })),
    rfDataSource: f.rfDataSource,
    showWhen: f.showWhen,
    amountChips: f.amountChips,
  };
}

function mapGatewayFieldType(type: string | null | undefined): GatewayFormField['type'] {
  const t = String(type || 'TEXT').toUpperCase();
  if (t === 'NUMBER' || t === 'MONEY' || t === 'INTEGER' || t === 'DECIMAL' || t === 'AMOUNT') {
    return 'number';
  }
  if (t === 'SELECT' || t === 'ENUM' || t === 'RADIO') {
    return 'select';
  }
  if (t === 'CHECKBOX' || t === 'BOOLEAN' || t === 'BOOL') {
    return 'checkbox';
  }
  if (t === 'EMAIL') {
    return 'email';
  }
  if (t === 'PHONE' || t === 'TEL' || t === 'MSISDN') {
    return 'tel';
  }
  if (t === 'TEXTAREA') {
    return 'textarea';
  }
  if (t === 'PASSWORD') {
    return 'password';
  }
  return 'text';
}

function mapSimpleType(type: string | null | undefined): GatewayFormField['type'] {
  return mapGatewayFieldType(type);
}
