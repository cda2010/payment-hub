
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { StepsModule } from 'primeng/steps';
import { ProviderService } from '../../core/service/provider.service';
import { ProviderEnvironmentCode, ProviderTemplate } from '../../core/model/api.models';
import { methodTypeLabel } from '../checkout/checkout.utils';

@Component({
  selector: 'app-provider-wizard',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule,
    StepsModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <a pButton class="p-button-text p-button-sm" routerLink="/admin/providers" label="← Fournisseurs"></a>
          <h1>Assistant — Nouveau fournisseur</h1>
          <p class="subtitle">Choisissez un modèle, saisissez l'URL réelle, puis configurez les détails.</p>
        </div>
      </div>

      @if (loading()) {
        <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
      } @else {
        <div class="gw-panel">
          <p-steps [model]="stepItems" [activeIndex]="activeIndex()" [readonly]="true" />

          @if (activeIndex() === 0) {
            <form [formGroup]="templateForm" class="step">
              <p class="subtitle">Sélectionnez un parcours pré-configuré (flow, APIs, mappings).</p>
              <div class="templates">
                @for (t of templates(); track t.code) {
                  <button type="button" class="template-card"
                    [class.selected]="templateForm.controls.templateCode.value === t.code"
                    (click)="pickTemplate(t)">
                    <strong>{{ t.libelle }}</strong>
                    <span>{{ t.description }}</span>
                    <span class="badge">{{ methodLabel(t.paymentMethodType) }} · {{ t.modeExecution }}</span>
                  </button>
                }
              </div>
              <div class="gw-actions">
                <button pButton type="button" label="Suivant" icon="pi pi-arrow-right" iconPos="right"
                  [disabled]="templateForm.invalid" (click)="next()"></button>
              </div>
            </form>
          }

          @if (activeIndex() === 1) {
            <form [formGroup]="detailsForm" class="step gw-form-grid">
              <div class="gw-field">
                <label>Code</label>
                <input pInputText formControlName="code" placeholder="CORIS_MONEY" class="w-full" />
              </div>
              <div class="gw-field">
                <label>Libellé</label>
                <input pInputText formControlName="libelle" class="w-full" />
              </div>
              <div class="gw-field full">
                <label>Description</label>
                <textarea pInputTextarea rows="2" formControlName="description" class="w-full"></textarea>
              </div>
              <div class="gw-field">
                <label>Ordre d'affichage</label>
                <input pInputText type="number" formControlName="ordreAffichage" class="w-full" />
              </div>
              <div class="gw-field">
                <label>Actif dans le checkout</label>
                <p-inputSwitch formControlName="actif" />
              </div>
              <div class="gw-actions full">
                <button pButton type="button" class="p-button-text" label="Retour" icon="pi pi-arrow-left" (click)="prev()"></button>
                <button pButton type="button" label="Suivant" icon="pi pi-arrow-right" iconPos="right"
                  [disabled]="detailsForm.invalid" (click)="next()"></button>
              </div>
            </form>
          }

          @if (activeIndex() === 2) {
            <form [formGroup]="envForm" class="step gw-form-grid">
              <div class="gw-field">
                <label>Code environnement</label>
                <p-dropdown
                  formControlName="environmentCode"
                  [options]="envCodeOptions"
                  optionLabel="label"
                  optionValue="value"
                  styleClass="w-full"
                />
              </div>
              <div class="gw-field full">
                <label>Base URL</label>
                <input pInputText formControlName="baseUrl" placeholder="https://api.example.com" class="w-full" />
                <small class="subtitle">Environnement actif à la création ; DEVELOPPEMENT et PRODUCTION sont ajoutés inactifs</small>
              </div>
              <div class="gw-actions full">
                <button pButton type="button" class="p-button-text" label="Retour" icon="pi pi-arrow-left" (click)="prev()"></button>
                <button pButton type="button" label="Créer le fournisseur" icon="pi pi-check"
                  [disabled]="envForm.invalid || saving()" [loading]="saving()" (click)="create()"></button>
              </div>
            </form>
          }
        </div>
        @if (error()) {
          <p class="gw-msg error">{{ error() }}</p>
        }
      }
    </div>
  `,
  styles: `
    .step { padding: 1rem 0 0; }
    .templates {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 0.75rem;
      margin-bottom: 1rem;
    }
    .template-card {
      display: flex; flex-direction: column; align-items: flex-start; gap: 0.35rem;
      padding: 1rem; border: 2px solid #e2e8f0; border-radius: 12px; background: #fff;
      cursor: pointer; text-align: left;
    }
    .template-card.selected {
      border-color: var(--primary-color, #2563eb);
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
    }
    .template-card span { color: #64748b; font-size: 0.875rem; }
    .badge {
      background: #eff6ff; color: #1d4ed8; padding: 0.15rem 0.5rem;
      border-radius: 999px; font-size: 0.75rem;
    }
    a[routerLink] { text-decoration: none; }
  `
})

export class ProviderWizardComponent implements OnInit {
  private readonly providersApi = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly templates = signal<ProviderTemplate[]>([]);
  readonly methodLabel = methodTypeLabel;

  readonly templateForm = this.fb.nonNullable.group({
    templateCode: ['', Validators.required]
  });

  readonly detailsForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    libelle: ['', Validators.required],
    description: [''],
    ordreAffichage: [0],
    actif: [false]
  });

  readonly envForm = this.fb.nonNullable.group({
    environmentCode: ['SANDBOX' as ProviderEnvironmentCode, Validators.required],
    baseUrl: ['', Validators.required]
  });

  readonly envCodeOptions: Array<{ label: string; value: ProviderEnvironmentCode }> = [
    { label: 'SANDBOX', value: 'SANDBOX' },
    { label: 'DEVELOPPEMENT', value: 'DEVELOPPEMENT' },
    { label: 'PRODUCTION', value: 'PRODUCTION' }
  ];

  ngOnInit(): void {
    this.providersApi.listTemplates().subscribe({
      next: (list) => {
        this.templates.set(list);
        this.loading.set(false);
        if (list.length) {
          this.pickTemplate(list[0]);
        }
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Impossible de charger les modèles');
        this.loading.set(false);
      }
    });
  }

  pickTemplate(template: ProviderTemplate): void {
    this.templateForm.patchValue({ templateCode: template.code });
    if (!this.detailsForm.controls.libelle.value) {
      this.detailsForm.patchValue({ libelle: template.libelle });
    }
    this.envForm.patchValue({ baseUrl: template.defaultBaseUrl });
  }

  create(): void {
    if (this.templateForm.invalid || this.detailsForm.invalid || this.envForm.invalid) {
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const t = this.templateForm.getRawValue();
    const d = this.detailsForm.getRawValue();
    const e = this.envForm.getRawValue();

    this.providersApi
      .createFromTemplate({
        templateCode: t.templateCode,
        code: d.code,
        libelle: d.libelle,
        description: d.description || null,
        environmentCode: e.environmentCode,
        baseUrl: e.baseUrl,
        actif: d.actif,
        ordreAffichage: d.ordreAffichage
      })
      .subscribe({
        next: (provider) => {
          this.saving.set(false);
          void this.router.navigate(['/admin/providers', provider.id, 'apis']);
        },
        error: (err: { message?: string }) => {
          this.error.set(err?.message ?? 'Création impossible');
          this.saving.set(false);
        }
      });
  }

  readonly activeIndex = signal(0);
  readonly stepItems: MenuItem[] = [
    { label: 'Modèle' },
    { label: 'Identité' },
    { label: 'Environnement' }
  ];

  next(): void {
    const i = this.activeIndex();
    if (i === 0 && this.templateForm.invalid) {
      return;
    }
    if (i === 1 && this.detailsForm.invalid) {
      return;
    }
    if (i < 2) {
      this.activeIndex.set(i + 1);
    }
  }

  prev(): void {
    const i = this.activeIndex();
    if (i > 0) {
      this.activeIndex.set(i - 1);
    }
  }

}
