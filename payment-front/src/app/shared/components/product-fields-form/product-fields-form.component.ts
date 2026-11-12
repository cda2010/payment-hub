import { Component, Input, OnChanges, inject, output } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProductField } from '../../../core/service/bill.service';

@Component({
  selector: 'app-product-fields-form',
  standalone: true,
  imports: [ReactiveFormsModule, DropdownModule, InputTextModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="fields-form">
      @for (field of fields; track field.name) {
        @if (field.type === 'select' && field.options?.length) {
          <div class="full">
            <label [attr.for]="field.name">{{ field.label }}</label>
            <p-dropdown
              [inputId]="field.name"
              [formControlName]="field.name"
              [options]="field.options ?? []"
              styleClass="w-full"
              placeholder="Sélectionner"
            />
          </div>
        } @else {
          <div class="full">
            <label [attr.for]="field.name">{{ field.label }}</label>
            <input
              pInputText
              [id]="field.name"
              [formControlName]="field.name"
              [type]="inputType(field)"
              [placeholder]="field.placeholder || ''"
              class="w-full"
            />
          </div>
        }
      }
      <ng-content />
    </form>
  `,
  styles: `
    .fields-form { display: flex; flex-direction: column; gap: 0.75rem; }
    .full { width: 100%; }
    .full label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; color: #334155; }
  `
})
export class ProductFieldsFormComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);

  @Input() fields: ProductField[] = [];
  @Input() defaults: Record<string, string | number> = {};

  readonly submitted = output<Record<string, unknown>>();

  form: FormGroup = this.fb.group({});

  ngOnChanges(): void {
    const group: Record<string, FormControl> = {};
    for (const field of this.fields ?? []) {
      const validators = [];
      if (field.required) {
        validators.push(Validators.required);
      }
      if (field.pattern) {
        validators.push(Validators.pattern(field.pattern));
      }
      const defaultValue =
        this.defaults[field.name] ??
        field.defaultValue ??
        (field.type === 'number' ? null : '');
      group[field.name] = new FormControl(defaultValue, validators);
    }
    this.form = this.fb.group(group);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit(this.form.getRawValue() as Record<string, unknown>);
  }

  submit(): void {
    this.onSubmit();
  }

  inputType(field: ProductField): string {
    const t = (field.type || 'text').toLowerCase();
    if (['number', 'tel', 'email', 'password'].includes(t)) {
      return t;
    }
    return 'text';
  }
}
