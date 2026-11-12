import { Component, Input, OnChanges, output, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-otp-input',
  standalone: true,
  imports: [InputTextModule, ButtonModule, MessageModule],
  template: `
    @if (message) {
      <p-message severity="info" [text]="message" styleClass="w-full mb" />
    }

    <div class="field">
      <label for="otpCode">Code OTP ({{ otpLength }} chiffres)</label>
      <input
        pInputText
        id="otpCode"
        type="text"
        inputmode="numeric"
        autocomplete="one-time-code"
        class="w-full"
        [value]="code()"
        [attr.maxlength]="otpLength"
        [disabled]="submitting"
        (input)="onInput($event)"
        (keydown.enter)="submit()"
      />
      @if (showError()) {
        <p-message
          severity="error"
          [text]="'Saisissez exactement ' + otpLength + ' chiffres'"
          styleClass="w-full mt"
        />
      }
    </div>

    <p class="hint">{{ code().length }} / {{ otpLength }} chiffres</p>

    <button
      pButton
      type="button"
      icon="pi pi-check"
      [disabled]="!canSubmit()"
      (click)="submit()"
      [label]="submitting ? 'Validation…' : 'Valider le code'"
    ></button>
  `,
  styles: `
    .field { width: 100%; margin-bottom: 0.5rem; }
    .field label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; }
    .hint { margin: 0 0 0.75rem; font-size: 0.875rem; color: var(--text-color-secondary); }
    :host ::ng-deep .w-full { width: 100%; }
    :host ::ng-deep .mb { margin-bottom: 0.75rem; display: block; }
    :host ::ng-deep .mt { margin-top: 0.5rem; display: block; }
  `,
})
export class OtpInputComponent implements OnChanges {
  @Input() message: string | null = null;
  @Input() length = 6;
  @Input() submitting = false;

  readonly otpSubmitted = output<string>();

  readonly code = signal('');
  readonly touched = signal(false);
  otpLength = 6;

  ngOnChanges(): void {
    this.otpLength = this.length > 0 ? this.length : 6;
    const trimmed = this.code().replace(/\D/g, '').slice(0, this.otpLength);
    if (trimmed !== this.code()) {
      this.code.set(trimmed);
    }
  }

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, this.otpLength);
    input.value = digits;
    this.code.set(digits);
    this.touched.set(true);
  }

  canSubmit(): boolean {
    return !this.submitting && this.code().length === this.otpLength;
  }

  showError(): boolean {
    return this.touched() && this.code().length > 0 && this.code().length !== this.otpLength;
  }

  submit(): void {
    this.touched.set(true);
    if (!this.canSubmit()) {
      return;
    }
    this.otpSubmitted.emit(this.code());
  }
}
