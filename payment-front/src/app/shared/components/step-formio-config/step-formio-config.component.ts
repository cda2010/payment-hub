import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  forwardRef,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { FormioModule } from '@formio/angular';
import {
  FormioSchema,
  emptyFormioSchema,
  parseConfigurationJsonToFormio,
  serializeFormioConfiguration,
} from '../../formio/formio-fields.builder';

/**
 * Admin editor for payment flow step configurationJson.
 * FORM steps → Form.io FormBuilder ; others → raw JSON textarea.
 */
@Component({
  selector: 'app-step-formio-config',
  standalone: true,
  imports: [FormioModule, ButtonModule, InputTextareaModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => StepFormioConfigComponent),
      multi: true,
    },
  ],
  template: `
    @if (mode === 'builder') {
      <div class="builder-wrap">
        <div class="builder-toolbar">
          <span class="hint">Formulaire Form.io (stocké dans configurationJson)</span>
          <button
            pButton
            type="button"
            class="p-button-text p-button-sm"
            [label]="showJson ? 'Masquer JSON' : 'Voir JSON'"
            (click)="toggleJson()"
          ></button>
          <button
            pButton
            type="button"
            class="p-button-text p-button-sm"
            label="Vider"
            (click)="resetEmpty()"
          ></button>
        </div>
        <div class="builder-host" [attr.data-render]="renderKey">
          <form-builder [form]="builderForm" (change)="onBuilderChange($event)"></form-builder>
        </div>
        @if (showJson) {
          <div class="gw-field full">
            <label>configurationJson</label>
            <textarea
              pInputTextarea
              rows="6"
              [value]="serialized"
              (input)="onJsonTyped($event)"
            ></textarea>
          </div>
        }
      </div>
    } @else {
      <div class="gw-field full">
        <label>configurationJson (optionnel)</label>
        <textarea
          pInputTextarea
          rows="3"
          [value]="rawValue"
          [disabled]="disabled"
          (input)="onRawTyped($event)"
        ></textarea>
      </div>
    }
  `,
  styles: `
    .builder-wrap {
      grid-column: 1 / -1;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 0.75rem;
      background: #f8fafc;
      margin-bottom: 0.5rem;
    }
    .builder-toolbar {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
      flex-wrap: wrap;
    }
    .hint {
      flex: 1;
      font-size: 0.85rem;
      color: #475569;
    }
    .builder-host {
      min-height: 220px;
      background: #fff;
      border-radius: 6px;
      padding: 0.5rem;
      overflow: auto;
    }
    .full {
      width: 100%;
      display: block;
      margin-top: 0.5rem;
    }
    :host ::ng-deep .formio-builder-form,
    :host ::ng-deep .formbuilder {
      min-height: 180px;
    }
  `,
})
export class StepFormioConfigComponent implements ControlValueAccessor, OnChanges {
  @Input() stepType: string | null = null;

  mode: 'builder' | 'raw' = 'raw';
  builderForm: FormioSchema = emptyFormioSchema();
  serialized = '';
  rawValue = '';
  showJson = false;
  renderKey = 0;
  disabled = false;

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['stepType']) {
      this.mode = this.stepType === 'FORM' ? 'builder' : 'raw';
      if (this.mode === 'builder') {
        this.syncBuilderFromRaw(this.rawValue, false);
      }
    }
  }

  writeValue(value: string | null): void {
    this.rawValue = value ?? '';
    if (this.mode === 'builder' || this.stepType === 'FORM') {
      this.mode = 'builder';
      this.syncBuilderFromRaw(this.rawValue, false);
    } else {
      this.mode = 'raw';
    }
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onBuilderChange(event: unknown): void {
    const form = extractBuilderForm(event);
    if (!form?.components) {
      return;
    }
    this.builderForm = {
      display: String(form.display ?? 'form'),
      components: form.components.map((c) => ({ ...c })),
    };
    this.serialized = serializeFormioConfiguration(this.builderForm);
    this.rawValue = this.serialized;
    this.onChange(this.serialized);
    this.onTouched();
  }

  onJsonTyped(event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.rawValue = value;
    try {
      this.builderForm = parseConfigurationJsonToFormio(value);
      this.serialized = serializeFormioConfiguration(this.builderForm);
      this.renderKey += 1;
      this.onChange(this.serialized);
    } catch {
      this.onChange(value);
    }
    this.onTouched();
  }

  onRawTyped(event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.rawValue = value;
    this.onChange(value);
    this.onTouched();
  }

  toggleJson(): void {
    this.showJson = !this.showJson;
    if (this.showJson) {
      this.serialized = serializeFormioConfiguration(this.builderForm);
    }
  }

  resetEmpty(): void {
    this.builderForm = emptyFormioSchema();
    this.serialized = serializeFormioConfiguration(this.builderForm);
    this.rawValue = this.serialized;
    this.renderKey += 1;
    this.onChange(this.serialized);
    this.onTouched();
  }

  private syncBuilderFromRaw(raw: string, emit = false): void {
    this.builderForm = parseConfigurationJsonToFormio(raw);
    this.serialized = serializeFormioConfiguration(this.builderForm);
    if (emit) {
      this.rawValue = this.serialized;
      this.onChange(this.serialized);
    }
    this.renderKey += 1;
  }
}

function extractBuilderForm(event: unknown): FormioSchema | null {
  if (!event || typeof event !== 'object') {
    return null;
  }
  const e = event as {
    form?: FormioSchema;
    builder?: { form?: FormioSchema; instance?: { form?: FormioSchema; schema?: FormioSchema } };
    components?: Record<string, unknown>[];
    display?: string;
  };
  if (e.form?.components) {
    return e.form;
  }
  if (e.builder?.form?.components) {
    return e.builder.form;
  }
  if (e.builder?.instance?.form?.components) {
    return e.builder.instance.form;
  }
  if (e.builder?.instance?.schema?.components) {
    return e.builder.instance.schema;
  }
  if (Array.isArray(e.components)) {
    return { display: String(e.display ?? 'form'), components: e.components };
  }
  return null;
}
