
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { WebhookConfig } from '../../core/model/api.models';

@Component({
  selector: 'app-provider-webhooks',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-page-header">
      <h2>Webhooks</h2>
      <button pButton type="button" class="p-button-outlined" label="Configurer" icon="pi pi-plus" (click)="showForm.set(!showForm())"></button>
    </div>

    @if (showForm()) {
      <div class="gw-panel">
        <form [formGroup]="form" (ngSubmit)="save()" class="gw-form-grid">
          <div class="gw-field">
            <label>Path suffix</label>
            <input pInputText formControlName="pathSuffix" placeholder="orange-money" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Mode auth</label>
            <input pInputText formControlName="authMode" placeholder="HMAC / API_KEY / NONE" class="w-full" />
          </div>
          <div class="gw-field">
            <label>HMAC secret</label>
            <input pInputText type="password" formControlName="hmacSecret" class="w-full" />
          </div>
          <div class="gw-field">
            <label>API key</label>
            <input pInputText type="password" formControlName="apiKey" class="w-full" />
          </div>
          <div class="gw-field full">
            <label>IPs autorisées</label>
            <input pInputText formControlName="allowedIps" placeholder="séparées par des virgules" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
          </div>
          <div class="gw-actions full">
            <button pButton type="submit" label="Enregistrer" [disabled]="form.invalid"></button>
          </div>
        </form>
      </div>
    }

    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else {
      <div class="gw-panel">
        <p-table [value]="items()" styleClass="p-datatable-sm">
          <ng-template pTemplate="header">
            <tr><th>Path</th><th>Auth</th><th>Secrets</th><th>Actif</th></tr>
          </ng-template>
          <ng-template pTemplate="body" let-w>
            <tr>
              <td>{{ w.pathSuffix }}</td>
              <td>{{ w.authMode || '—' }}</td>
              <td>
                {{ w.hasHmacSecret ? 'HMAC' : '' }}
                {{ w.hasApiKey ? 'API_KEY' : '' }}
                {{ !w.hasHmacSecret && !w.hasApiKey ? '—' : '' }}
              </td>
              <td>{{ w.actif ? 'Oui' : 'Non' }}</td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="4">Aucun webhook</td></tr>
          </ng-template>
        </p-table>
      </div>
    }
    @if (msg()) { <p class="gw-msg">{{ msg() }}</p> }
  `
})

export class ProviderWebhooksComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly columns = ['pathSuffix', 'authMode', 'secrets', 'actif'];
  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly items = signal<WebhookConfig[]>([]);
  readonly msg = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    pathSuffix: ['', Validators.required],
    authMode: ['HMAC'],
    hmacSecret: [''],
    apiKey: [''],
    allowedIps: [''],
    actif: [true]
  });

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.api.listWebhooks(this.providerId).subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.loading.set(false);
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue();
    this.api
      .upsertWebhook(this.providerId, {
        pathSuffix: v.pathSuffix,
        authMode: v.authMode || null,
        hmacSecret: v.hmacSecret || null,
        apiKey: v.apiKey || null,
        allowedIps: v.allowedIps || null,
        actif: v.actif
      })
      .subscribe({
        next: () => {
          this.showForm.set(false);
          this.msg.set('Webhook enregistré');
          this.reload();
        },
        error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
      });
  }
}
