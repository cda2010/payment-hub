
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ProviderService } from '../../core/service/provider.service';
import { AuthType, Environment, OtpConfig } from '../../core/model/api.models';

@Component({
  selector: 'app-provider-authentication',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <h2>Authentification</h2>
    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else {
      <div class="gw-panel">
        <h3>Identifiants fournisseur</h3>
        <form [formGroup]="authForm" (ngSubmit)="saveAuth()" class="gw-form-grid">
          <div class="gw-field">
            <label>Environnement</label>
            <p-dropdown
              formControlName="environmentId"
              [options]="environmentOptions"
              optionLabel="label"
              optionValue="value"
              styleClass="w-full"
              (onChange)="loadAuth()"
            />
          </div>
          <div class="gw-field">
            <label>Type d'auth</label>
            <p-dropdown formControlName="authType" [options]="authTypes" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Client ID</label>
            <input pInputText formControlName="clientId" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Client secret</label>
            <input pInputText type="password" formControlName="clientSecret" class="w-full" />
          </div>
          <div class="gw-field">
            <label>API key</label>
            <input pInputText type="password" formControlName="apiKey" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Token URL</label>
            <input pInputText formControlName="tokenUrl" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Username</label>
            <input pInputText formControlName="username" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Password</label>
            <input pInputText type="password" formControlName="password" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Custom header name</label>
            <input pInputText formControlName="customHeaderName" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Custom header value</label>
            <input pInputText formControlName="customHeaderValue" class="w-full" />
          </div>
          <div class="gw-actions full">
            <button pButton type="submit" label="Enregistrer auth" [disabled]="authForm.invalid"></button>
          </div>
        </form>
        @if (authHints()) {
          <p class="subtitle">{{ authHints() }}</p>
        }
      </div>

      <div class="gw-panel">
        <h3>Configuration OTP</h3>
        <form [formGroup]="otpForm" (ngSubmit)="saveOtp()" class="gw-form-grid">
          <div class="gw-field">
            <label>OTP requis</label>
            <p-inputSwitch formControlName="otpRequired" />
          </div>
          <div class="gw-field">
            <label>Renvoi autorisé</label>
            <p-inputSwitch formControlName="resendAllowed" />
          </div>
          <div class="gw-field">
            <label>Longueur</label>
            <input pInputText type="number" formControlName="length" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Validité (s)</label>
            <input pInputText type="number" formControlName="validitySeconds" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Tentatives max</label>
            <input pInputText type="number" formControlName="maxAttempts" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Délai renvoi (s)</label>
            <input pInputText type="number" formControlName="resendDelaySeconds" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Opération génération</label>
            <input pInputText formControlName="generateOperation" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Opération validation</label>
            <input pInputText formControlName="validateOperation" class="w-full" />
          </div>
          <div class="gw-actions full">
            <button pButton type="submit" label="Enregistrer OTP"></button>
          </div>
        </form>
      </div>
    }
    @if (msg()) { <p class="gw-msg">{{ msg() }}</p> }
  `
})

export class ProviderAuthenticationComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly authTypes: AuthType[] = [
    'NONE',
    'BASIC',
    'API_KEY',
    'BEARER_TOKEN',
    'OAUTH2_CLIENT_CREDENTIALS',
    'CUSTOM_HEADER',
    'MTLS'
  ];
  readonly loading = signal(true);
  readonly environments = signal<Environment[]>([]);
  readonly msg = signal<string | null>(null);
  readonly authHints = signal<string | null>(null);

  readonly authForm = this.fb.group({
    environmentId: ['', Validators.required],
    authType: ['API_KEY' as AuthType, Validators.required],
    clientId: [''],
    clientSecret: [''],
    apiKey: [''],
    tokenUrl: [''],
    username: [''],
    password: [''],
    customHeaderName: [''],
    customHeaderValue: ['']
  });

  readonly otpForm = this.fb.nonNullable.group({
    otpRequired: [false],
    length: [6],
    validitySeconds: [300],
    maxAttempts: [3],
    resendAllowed: [true],
    resendDelaySeconds: [60],
    generateOperation: [''],
    validateOperation: ['']
  });

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.api.listEnvironments(this.providerId).subscribe({
      next: (envs) => {
        this.environments.set(envs);
        if (envs.length) {
          this.authForm.patchValue({ environmentId: envs[0].id });
          this.loadAuth();
        }
        this.loadOtp();
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.loading.set(false);
      }
    });
  }

  loadAuth(): void {
    const envId = this.authForm.value.environmentId;
    if (!envId) {
      return;
    }
    this.api.getAuthentication(this.providerId, envId).subscribe({
      next: (cfg) => {
        this.authForm.patchValue({
          authType: cfg.authType,
          clientId: cfg.clientId ?? '',
          tokenUrl: cfg.tokenUrl ?? '',
          customHeaderName: cfg.customHeaderName ?? ''
        });
        const hints: string[] = [];
        if (cfg.hasClientSecret) hints.push('clientSecret configuré');
        if (cfg.hasApiKey) hints.push('apiKey configurée');
        if (cfg.hasUsername) hints.push('username configuré');
        if (cfg.hasPassword) hints.push('password configuré');
        this.authHints.set(hints.length ? `Secrets existants : ${hints.join(', ')}` : null);
      },
      error: () => this.authHints.set(null)
    });
  }

  loadOtp(): void {
    this.api.getOtpConfig(this.providerId).subscribe({
      next: (otp: OtpConfig) => {
        this.otpForm.patchValue({
          otpRequired: otp.otpRequired,
          length: otp.length ?? 6,
          validitySeconds: otp.validitySeconds ?? 300,
          maxAttempts: otp.maxAttempts ?? 3,
          resendAllowed: otp.resendAllowed,
          resendDelaySeconds: otp.resendDelaySeconds ?? 60,
          generateOperation: otp.generateOperation ?? '',
          validateOperation: otp.validateOperation ?? ''
        });
      },
      error: () => undefined
    });
  }

  saveAuth(): void {
    if (this.authForm.invalid) {
      return;
    }
    const v = this.authForm.getRawValue();
    this.api
      .upsertAuthentication(this.providerId, {
        environmentId: v.environmentId!,
        authType: v.authType as AuthType,
        clientId: v.clientId || null,
        clientSecret: v.clientSecret || null,
        apiKey: v.apiKey || null,
        tokenUrl: v.tokenUrl || null,
        username: v.username || null,
        password: v.password || null,
        customHeaderName: v.customHeaderName || null,
        customHeaderValue: v.customHeaderValue || null
      })
      .subscribe({
        next: () => {
          this.msg.set('Authentification enregistrée');
          this.loadAuth();
        },
        error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
      });
  }

  saveOtp(): void {
    const v = this.otpForm.getRawValue();
    this.api
      .upsertOtpConfig(this.providerId, {
        otpRequired: v.otpRequired,
        length: v.length,
        validitySeconds: v.validitySeconds,
        maxAttempts: v.maxAttempts,
        resendAllowed: v.resendAllowed,
        resendDelaySeconds: v.resendDelaySeconds,
        generateOperation: v.generateOperation || null,
        validateOperation: v.validateOperation || null
      })
      .subscribe({
        next: () => this.msg.set('Configuration OTP enregistrée'),
        error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
      });
  }

  get environmentOptions(): { label: string; value: string }[] {
    return this.environments().map((e) => ({ label: e.code, value: e.id }));
  }

}
