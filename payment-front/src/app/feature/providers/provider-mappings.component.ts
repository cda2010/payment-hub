
import { Component, OnInit, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { I18nApiService, SupportedLanguage } from '../../core/service/i18n-api.service';
import { PaymentStatus, StatusMapping } from '../../core/model/api.models';

@Component({
  selector: 'app-provider-mappings',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    UpperCasePipe,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-page-header">
      <h2>Mappings de statut</h2>
      <button pButton type="button" class="p-button-outlined" label="Ajouter" icon="pi pi-plus" (click)="startCreate()"></button>
    </div>

    @if (showForm()) {
      <div class="gw-panel">
        <h3>{{ editingId() ? 'Modifier le mapping' : 'Nouveau mapping' }}</h3>
        <form [formGroup]="form" (ngSubmit)="save()" class="gw-form-grid">
          <div class="gw-field">
            <label>Statut fournisseur</label>
            <input pInputText formControlName="providerStatus" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Statut gateway</label>
            <p-dropdown formControlName="gatewayStatus" [options]="statuses" styleClass="w-full" />
          </div>
          @for (lang of languages(); track lang.code) {
            <div class="gw-field">
              <label>Message ({{ lang.label }})</label>
              <input pInputText [formControlName]="'msg_' + lang.code" class="w-full" [placeholder]="lang.code" />
            </div>
          }
          <div class="gw-actions full">
            <button pButton type="button" class="p-button-outlined" label="Annuler" (click)="cancelEdit()"></button>
            <button pButton type="submit" [label]="editingId() ? 'Enregistrer' : 'Créer'" [disabled]="form.invalid"></button>
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
            <tr>
              <th>Fournisseur</th>
              <th>Gateway</th>
              @for (lang of languages(); track lang.code) {
                <th>Message {{ lang.code | uppercase }}</th>
              }
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-m>
            <tr>
              <td>{{ m.providerStatus }}</td>
              <td>{{ m.gatewayStatus }}</td>
              @for (lang of languages(); track lang.code) {
                <td>{{ m.messages?.[lang.code] || '—' }}</td>
              }
              <td class="gw-row-actions-cell">
                <button
                  pButton
                  type="button"
                  icon="pi pi-pencil"
                  class="p-button-rounded p-button-text"
                  title="Modifier"
                  aria-label="Modifier"
                  (click)="startEdit(m)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td [attr.colspan]="3 + languages().length">Aucun mapping</td></tr>
          </ng-template>
        </p-table>
      </div>
    }
    @if (msg()) { <p class="gw-msg">{{ msg() }}</p> }
  `,
  styles: `
    .gw-row-actions-cell {
      width: 3.5rem;
      text-align: center;
      white-space: nowrap;
    }
  `
})

export class ProviderMappingsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly i18nApi = inject(I18nApiService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly statuses: PaymentStatus[] = [
    'CREATED',
    'INITIATED',
    'AWAITING_USER_INPUT',
    'OTP_REQUIRED',
    'PROCESSING',
    'PENDING',
    'SUCCESS',
    'FAILED',
    'CANCELLED',
    'EXPIRED',
    'UNKNOWN'
  ];
  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly items = signal<StatusMapping[]>([]);
  readonly languages = signal<SupportedLanguage[]>([]);
  readonly msg = signal<string | null>(null);

  form: FormGroup = this.fb.group({
    providerStatus: ['', Validators.required],
    gatewayStatus: ['PENDING' as PaymentStatus, Validators.required]
  });

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.i18nApi.languages().subscribe({
      next: (res) => {
        this.languages.set(res.languages?.length ? res.languages : [
          { code: 'fr', label: 'Français' },
          { code: 'en', label: 'English' }
        ]);
        this.rebuildMessageControls();
        this.reload();
      },
      error: () => {
        this.languages.set([
          { code: 'fr', label: 'Français' },
          { code: 'en', label: 'English' }
        ]);
        this.rebuildMessageControls();
        this.reload();
      }
    });
  }

  private rebuildMessageControls(): void {
    const group: Record<string, FormControl<string>> = {
      providerStatus: this.fb.nonNullable.control('', Validators.required),
      gatewayStatus: this.fb.nonNullable.control('PENDING' as PaymentStatus, Validators.required)
    };
    for (const lang of this.languages()) {
      group['msg_' + lang.code] = this.fb.nonNullable.control('');
    }
    this.form = this.fb.group(group);
  }

  reload(): void {
    this.loading.set(true);
    this.api.listStatusMappings(this.providerId).subscribe({
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

  startCreate(): void {
    this.editingId.set(null);
    this.resetForm();
    this.showForm.set(true);
  }

  startEdit(m: StatusMapping): void {
    this.editingId.set(m.id);
    const patch: Record<string, string> = {
      providerStatus: m.providerStatus,
      gatewayStatus: m.gatewayStatus
    };
    for (const lang of this.languages()) {
      patch['msg_' + lang.code] = m.messages?.[lang.code] ?? '';
    }
    this.form.reset(patch);
    this.showForm.set(true);
  }

  cancelEdit(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  private resetForm(): void {
    const patch: Record<string, string> = {
      providerStatus: '',
      gatewayStatus: 'PENDING'
    };
    for (const lang of this.languages()) {
      patch['msg_' + lang.code] = '';
    }
    this.form.reset(patch);
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue() as Record<string, string>;
    const messages: Record<string, string> = {};
    for (const lang of this.languages()) {
      const text = (v['msg_' + lang.code] ?? '').trim();
      if (text) {
        messages[lang.code] = text;
      }
    }
    const body = {
      providerStatus: v['providerStatus'],
      gatewayStatus: v['gatewayStatus'] as PaymentStatus,
      messages: Object.keys(messages).length ? messages : null
    };
    const id = this.editingId();
    const req$ = id
      ? this.api.updateStatusMapping(this.providerId, id, body)
      : this.api.createStatusMapping(this.providerId, body);
    req$.subscribe({
      next: () => {
        this.cancelEdit();
        this.reload();
      },
      error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
    });
  }
}
