import { Component, Input, OnChanges, OnDestroy, SimpleChanges, inject, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { FormioModule } from '@formio/angular';
import { Subscription } from 'rxjs';
import { FormField, FormioSchemaDto, Payment } from '../../../core/model/api.models';
import { GatewayService } from '../../../core/service/gateway.service';
import {
  FormioSchema,
  formioDataToPaymentData,
  resolveFormioSchema,
} from '../../formio/formio-fields.builder';
import {
  buildDataSourceQuery,
  dependsOnList,
  extractRfDataSource,
  parentsReady,
} from '../../formio/rf-datasource.util';

/**
 * Host Form.io — gère :
 * A) logique native Formio (customConditional, calculateValue, redrawOn)
 * B) cascade rfDataSource.dependsOn → GET /data-sources/{code}
 */
@Component({
  selector: 'app-dynamic-form',
  standalone: true,
  imports: [FormioModule, ButtonModule],
  template: `
    @if (message) {
      <p class="msg">{{ message }}</p>
    }
    @if (formSchema) {
      <div class="formio-host" [attr.data-render]="formRenderKey">
        <formio
          [form]="formSchema"
          [submission]="formSubmission"
          (change)="onFormChange($event)"
        ></formio>
      </div>
    }
    <button
      pButton
      type="button"
      label="Continuer"
      (click)="onSubmit()"
      [disabled]="!canSubmit || submitting"
    ></button>
  `,
  styles: `
    .msg {
      margin-bottom: 0.75rem;
      color: #334155;
    }
    .formio-host {
      margin-bottom: 0.75rem;
    }
    :host ::ng-deep .formio-component-submit,
    :host ::ng-deep button[type='submit'] {
      display: none !important;
    }
  `,
})
export class DynamicFormComponent implements OnChanges, OnDestroy {
  private readonly gateway = inject(GatewayService);

  @Input() fields: FormField[] = [];
  /** Phase 2 — schéma Form.io natif depuis nextAction.formSchema */
  @Input() apiFormSchema: FormioSchemaDto | null = null;
  @Input() payment: Payment | null = null;
  @Input() message: string | null = null;
  @Input() submitting = false;

  readonly submitted = output<Record<string, unknown>>();

  formSchema: FormioSchema | null = null;
  formSubmission: { data: Record<string, unknown> } = { data: {} };
  formRenderKey = 0;
  canSubmit = false;

  private formData: Record<string, unknown> = {};
  private formValid = false;
  private fetchSubs: Subscription[] = [];
  private lastChangedKey: string | null = null;
  private cascadeBusy = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fields'] || changes['payment'] || changes['apiFormSchema']) {
      this.rebuildForm();
    }
  }

  ngOnDestroy(): void {
    this.fetchSubs.forEach((s) => s.unsubscribe());
  }

  onFormChange(event: {
    data?: Record<string, unknown>;
    isValid?: boolean;
    changed?: { instance?: { key?: string }; component?: { key?: string } };
  }): void {
    if (!event?.data || this.cascadeBusy) {
      return;
    }
    const prev = this.formData;
    this.formData = { ...event.data };
    this.formValid = event.isValid !== false;
    this.canSubmit = this.formValid;
    this.formSubmission = { data: { ...this.formData } };

    const changedKey =
      event.changed?.instance?.key ||
      event.changed?.component?.key ||
      this.detectChangedKey(prev, this.formData);
    if (changedKey) {
      this.lastChangedKey = changedKey;
      this.refreshDependents(changedKey);
    }
  }

  onSubmit(): void {
    if (!this.canSubmit || this.submitting) {
      return;
    }
    const data = formioDataToPaymentData(this.formData);
    for (const field of this.fields ?? []) {
      if (this.isAlreadyCollected(field.name) && data[field.name] === undefined) {
        data[field.name] = this.resolveExistingValue(field.name);
      }
    }
    this.submitted.emit(data);
  }

  private rebuildForm(): void {
    this.fetchSubs.forEach((s) => s.unsubscribe());
    this.fetchSubs = [];
    this.formSchema = resolveFormioSchema(
      this.apiFormSchema,
      this.fields,
      (name) => this.isAlreadyCollected(name),
    );
    const visibleCount = (this.formSchema.components ?? []).filter(
      (c) => c['key'] !== 'submit' && c['type'] !== 'button' && !c['hidden'],
    ).length;
    this.formData = {};
    this.formSubmission = { data: {} };
    this.formValid = visibleCount === 0;
    this.canSubmit = visibleCount === 0;
    this.formRenderKey += 1;
  }

  private refreshDependents(changedKey: string): void {
    if (!this.formSchema?.components) {
      return;
    }
    const targets = collectDependentComponents(this.formSchema.components, changedKey);
    for (const { comp, rf } of targets) {
      if (!parentsReady(rf, this.formData)) {
        patchSelectValues(comp, []);
        this.bumpRender();
        continue;
      }
      const query = buildDataSourceQuery(rf, this.formData);
      const product = this.payment?.catalogServiceCode;
      if (product && !query['productCode']) {
        query['productCode'] = String(product);
      }
      const key = String(comp['key'] ?? '');
      const sub = this.gateway.resolveDataSource(rf.code, query).subscribe({
        next: (resp) => {
          const values = (resp.items ?? []).map((it) => ({
            label: it.label || it.id,
            value: String(it.value ?? it.id),
          }));
          patchSelectValues(comp, values);
          if (key && this.formData[key] != null) {
            this.formData = { ...this.formData, [key]: '' };
            this.formSubmission = { data: { ...this.formData } };
          }
          this.bumpRender();
        },
        error: () => {
          patchSelectValues(comp, []);
          this.bumpRender();
        },
      });
      this.fetchSubs.push(sub);
    }
  }

  private bumpRender(): void {
    this.cascadeBusy = true;
    this.formRenderKey += 1;
    setTimeout(() => {
      this.cascadeBusy = false;
    }, 0);
  }

  private detectChangedKey(
    prev: Record<string, unknown>,
    next: Record<string, unknown>,
  ): string | null {
    const keys = new Set([...Object.keys(prev), ...Object.keys(next)]);
    for (const k of keys) {
      if (String(prev[k] ?? '') !== String(next[k] ?? '')) {
        return k;
      }
    }
    return this.lastChangedKey;
  }

  private isAlreadyCollected(name: string): boolean {
    const p = this.payment;
    if (!p) {
      return false;
    }
    switch (name) {
      case 'amount':
        return p.amount != null;
      case 'currency':
        return !!p.currency;
      case 'phoneNumber':
      case 'phone':
        return !!p.phoneNumber;
      case 'customerReference':
        return !!p.customerReference;
      default:
        return false;
    }
  }

  private resolveExistingValue(name: string): unknown {
    const p = this.payment;
    if (!p) {
      return undefined;
    }
    switch (name) {
      case 'amount':
        return p.amount;
      case 'currency':
        return p.currency;
      case 'phoneNumber':
      case 'phone':
        return p.phoneNumber;
      case 'customerReference':
        return p.customerReference;
      default:
        return undefined;
    }
  }
}

function collectDependentComponents(
  components: Record<string, unknown>[],
  changedKey: string,
): Array<{ comp: Record<string, unknown>; rf: NonNullable<ReturnType<typeof extractRfDataSource>> }> {
  const out: Array<{
    comp: Record<string, unknown>;
    rf: NonNullable<ReturnType<typeof extractRfDataSource>>;
  }> = [];
  const walk = (list: Record<string, unknown>[]) => {
    for (const c of list) {
      const nested = c['components'] as Record<string, unknown>[] | undefined;
      if (Array.isArray(nested)) {
        walk(nested);
      }
      const rf = extractRfDataSource(c);
      if (rf && dependsOnList(rf).includes(changedKey)) {
        out.push({ comp: c, rf });
      }
    }
  };
  walk(components);
  return out;
}

function patchSelectValues(
  comp: Record<string, unknown>,
  values: Array<{ label: string; value: string }>,
): void {
  const data =
    comp['data'] && typeof comp['data'] === 'object'
      ? { ...(comp['data'] as Record<string, unknown>) }
      : {};
  data['values'] = values;
  comp['data'] = data;
  comp['dataSrc'] = 'values';
}
