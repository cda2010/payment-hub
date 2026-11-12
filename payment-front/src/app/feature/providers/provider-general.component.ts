
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ProviderService } from '../../core/service/provider.service';
import {
  ExecutionMode,
  FseParticipantKind,
  FseStatut,
  PaymentMethodType,
  ServiceType,
  ServiceTypeCode
} from '../../core/model/api.models';
import { methodTypeLabel } from '../checkout/checkout.utils';

@Component({
  selector: 'app-provider-general',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    MultiSelectModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else {
      <div class="gw-panel">
        <h2>Informations générales</h2>
        <form [formGroup]="form" (ngSubmit)="save()" class="gw-form-grid">
          <div class="gw-field">
            <label>Code</label>
            <input pInputText formControlName="code" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Libellé</label>
            <input pInputText formControlName="libelle" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Type de moyen</label>
            <p-dropdown formControlName="paymentMethodType" [options]="methodTypeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Mode d'exécution</label>
            <p-dropdown formControlName="modeExecution" [options]="modes" styleClass="w-full" />
          </div>
          <div class="gw-field full">
            <label>Types de service</label>
            <p-multiSelect
              formControlName="serviceTypes"
              [options]="serviceTypeOptions()"
              optionLabel="label"
              optionValue="value"
              display="chip"
              placeholder="LINK, TRANSFERT, PAYEMENT…"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label>Ordre</label>
            <input pInputText type="number" formControlName="ordreAffichage" class="w-full" />
          </div>
          <div class="gw-field full">
            <label>Description</label>
            <textarea pInputTextarea rows="2" formControlName="description" class="w-full"></textarea>
          </div>
          <div class="gw-field full">
            <label>URL logo</label>
            <input pInputText formControlName="logoUrl" class="w-full" />
          </div>

          <div class="gw-field full"><h3 class="gw-section-title">Identité FSE (optionnel)</h3></div>
          <div class="gw-field">
            <label>Code FSE</label>
            <input pInputText formControlName="codeFse" class="w-full" placeholder="ex. 0011" />
          </div>
          <div class="gw-field">
            <label>Code externe</label>
            <input pInputText formControlName="codeExterne" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Groupe</label>
            <input pInputText formControlName="groupCode" class="w-full" placeholder="ex. CORIS" />
          </div>
          <div class="gw-field">
            <label>Pays</label>
            <p-dropdown
              formControlName="countryCode"
              [options]="paysOptions()"
              optionLabel="label"
              optionValue="value"
              [filter]="true"
              filterBy="label"
              placeholder="Choisir un pays"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label>Type fournisseur</label>
            <p-dropdown formControlName="participantKind" [options]="kindOptions" optionLabel="label" optionValue="value" [showClear]="true" placeholder="—" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Statut FSE</label>
            <p-dropdown formControlName="fseStatut" [options]="statutOptions" optionLabel="label" optionValue="value" [showClear]="true" placeholder="—" styleClass="w-full" />
          </div>

          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
          </div>
          <div class="gw-actions full">
            <button pButton type="submit" label="Enregistrer" [disabled]="form.invalid || saving()" [loading]="saving()"></button>
          </div>
        </form>
        @if (msg()) {
          <p class="gw-msg" [class.error]="isError()">{{ msg() }}</p>
        }
      </div>
    }
  `,
  styles: `
    .gw-section-title {
      margin: 0.5rem 0 0;
      font-size: 0.95rem;
      font-weight: 600;
      color: var(--text-color-secondary, #64748b);
    }
  `
})

export class ProviderGeneralComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly providersApi = inject(ProviderService);
  private readonly fb = inject(FormBuilder);

  private providerId = '';
  readonly modes: ExecutionMode[] = ['OTP', 'REDIRECT', 'PUSH', 'USSD', 'DIRECT', 'ASYNC'];
  readonly methodTypes: PaymentMethodType[] = ['MOBILE_MONEY', 'CARD', 'BANK_TRANSFER', 'OTHER'];
  readonly methodLabel = methodTypeLabel;
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly msg = signal<string | null>(null);
  readonly isError = signal(false);
  readonly serviceTypeOptions = signal<Array<{ label: string; value: ServiceTypeCode }>>([]);
  readonly paysOptions = signal<Array<{ label: string; value: string }>>([]);

  readonly kindOptions: Array<{ label: string; value: FseParticipantKind }> = [
    { label: 'Wallet', value: 'WALLET' },
    { label: 'Banque', value: 'BANQUE' },
    { label: 'Facturier', value: 'FACTURIER' },
    { label: 'Remittance', value: 'REMITTANCE' },
    { label: 'Réseau / rail', value: 'RESEAU' }
  ];
  readonly statutOptions: Array<{ label: string; value: FseStatut }> = [
    { label: 'Banque', value: 'BANQUE' },
    { label: 'Non-banque', value: 'NONBANQUE' },
    { label: 'Autre', value: 'AUTRE' }
  ];

  readonly form = this.fb.nonNullable.group({
    code: ['', Validators.required],
    libelle: ['', Validators.required],
    description: [''],
    logoUrl: [''],
    ordreAffichage: [0],
    paymentMethodType: ['MOBILE_MONEY' as PaymentMethodType, Validators.required],
    modeExecution: ['DIRECT' as ExecutionMode, Validators.required],
    serviceTypes: [[] as ServiceTypeCode[]],
    codeFse: [''],
    codeExterne: [''],
    groupCode: [''],
    countryCode: [''],
    participantKind: [null as FseParticipantKind | null],
    fseStatut: [null as FseStatut | null],
    actif: [true]
  });

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.providersApi.listPays().subscribe({
      next: (rows) => {
        const options = rows
          .filter((row) => row.actif)
          .map((row) => ({ label: `${row.nom} (${row.code})`, value: row.nom }));
        this.paysOptions.set(options);
        this.keepCurrentPays(this.form.controls.countryCode.value);
      }
    });
    this.providersApi.listServiceTypes().subscribe({
      next: (types: ServiceType[]) => {
        this.serviceTypeOptions.set(
          types.map((t) => ({ label: `${t.code} — ${t.libelle}`, value: t.code }))
        );
      }
    });
    this.providersApi.get(this.providerId).subscribe({
      next: (p) => {
        this.form.patchValue({
          code: p.code,
          libelle: p.libelle,
          description: p.description ?? '',
          logoUrl: p.logoUrl ?? '',
          ordreAffichage: p.ordreAffichage ?? 0,
          paymentMethodType: p.paymentMethodType ?? 'MOBILE_MONEY',
          modeExecution: p.modeExecution,
          serviceTypes: p.serviceTypes ?? [],
          codeFse: p.codeFse ?? '',
          codeExterne: p.codeExterne ?? '',
          groupCode: p.groupCode ?? '',
          countryCode: p.countryCode ?? '',
          participantKind: p.participantKind ?? null,
          fseStatut: p.fseStatut ?? null,
          actif: p.actif
        });
        this.keepCurrentPays(p.countryCode ?? '');
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.isError.set(true);
        this.loading.set(false);
      }
    });
  }

  /** Garde le pays déjà enregistré s'il n'est pas dans le référentiel. */
  private keepCurrentPays(nom: string): void {
    const value = nom.trim();
    if (!value || this.paysOptions().some((option) => option.value === value)) {
      return;
    }
    this.paysOptions.update((options) => [{ label: value, value }, ...options]);
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue();
    this.providersApi
      .update(this.providerId, {
        code: v.code,
        libelle: v.libelle,
        description: v.description || null,
        logoUrl: v.logoUrl || null,
        actif: v.actif,
        ordreAffichage: v.ordreAffichage,
        paymentMethodType: v.paymentMethodType,
        modeExecution: v.modeExecution,
        serviceTypes: v.serviceTypes,
        codeFse: v.codeFse || null,
        codeExterne: v.codeExterne || null,
        groupCode: v.groupCode || null,
        countryCode: v.countryCode || null,
        participantKind: v.participantKind,
        fseStatut: v.fseStatut
      })
      .subscribe({
        next: () => {
          this.msg.set('Enregistré');
          this.isError.set(false);
          this.saving.set(false);
        },
        error: (err: { message?: string }) => {
          this.msg.set(err?.message ?? 'Erreur');
          this.isError.set(true);
          this.saving.set(false);
        }
      });
  }

  readonly methodTypeOptions = this.methodTypes.map((t) => ({ label: methodTypeLabel(t), value: t }));

}
