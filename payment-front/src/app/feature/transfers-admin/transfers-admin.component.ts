import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TabViewModule } from 'primeng/tabview';
import {
  FeePolicyType,
  FeeRule,
  FeeRuleLine,
  ParticipantKind,
  RailScope,
  TransferFeePolicy,
  TransferField,
  TransferProvider,
  TransferRail,
  TransferService
} from '../../core/service/transfer.service';

@Component({
  selector: 'app-transfers-admin',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CheckboxModule,
    DropdownModule,
    InputTextModule,
    InputTextareaModule,
    ProgressSpinnerModule,
    TabViewModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Transferts</h1>
          <p class="subtitle">Rails, fournisseurs (émetteur / bénéficiaire) et politiques de frais</p>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <p-tabView styleClass="gw-tabs-nav">
        <p-tabPanel header="Rails">
          <div class="layout">
            <aside class="list-panel gw-panel">
              @if (loading()) {
                <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
              } @else {
                <button pButton type="button" label="+ Nouveau rail" class="p-button-text" (click)="startNewRail()"></button>
                <ul>
                  @for (r of rails(); track r.id) {
                    <li [class.active]="selectedRail()?.id === r.id" (click)="selectRail(r)">
                      <strong>{{ r.name }}</strong>
                      <span>{{ r.code }} · {{ r.railScope }}{{ r.actif ? '' : ' · inactif' }}</span>
                    </li>
                  }
                </ul>
              }
            </aside>
            <section class="detail-panel gw-panel">
              @if (editingRail()) {
                <h2>{{ selectedRail() ? 'Modifier' : 'Nouveau' }} rail</h2>
                <form [formGroup]="railForm" (ngSubmit)="saveRail()" class="gw-form-grid">
                  <div class="gw-field">
                    <label>Code</label>
                    <input pInputText formControlName="code" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Nom</label>
                    <input pInputText formControlName="name" class="w-full" />
                  </div>
                  <div class="gw-field full">
                    <label>Description</label>
                    <textarea pInputTextarea rows="2" formControlName="description" class="w-full"></textarea>
                  </div>
                  <div class="gw-field">
                    <label>Portée</label>
                    <p-dropdown formControlName="railScope" [options]="railScopeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Devise</label>
                    <input pInputText formControlName="currency" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Min</label>
                    <input pInputText type="number" formControlName="minAmount" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Max</label>
                    <input pInputText type="number" formControlName="maxAmount" class="w-full" />
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="requiresBank" [binary]="true" inputId="requiresBank" />
                    <label for="requiresBank">Nécessite une banque</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="sameCountryOnly" [binary]="true" inputId="sameCountryOnly" />
                    <label for="sameCountryOnly">Même pays uniquement</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="sameGroupOnly" [binary]="true" inputId="sameGroupOnly" />
                    <label for="sameGroupOnly">Même groupe uniquement</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="crossBorderOnly" [binary]="true" inputId="crossBorderOnly" />
                    <label for="crossBorderOnly">Transfrontalier uniquement</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="actif" [binary]="true" inputId="railActif" />
                    <label for="railActif">Actif</label>
                  </div>
                  <div class="gw-field full">
                    <label>Champs supplémentaires (JSON)</label>
                    <textarea pInputTextarea rows="5" formControlName="extraFieldsJson" class="w-full"></textarea>
                  </div>
                  <div class="gw-actions full">
                    <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelRail()"></button>
                    <button pButton type="submit" label="Enregistrer" [disabled]="busy() || railForm.invalid"></button>
                  </div>
                </form>
              } @else if (selectedRail()) {
                <header class="detail-head">
                  <div>
                    <h2>{{ selectedRail()!.name }}</h2>
                    <p>{{ selectedRail()!.code }} · {{ selectedRail()!.railScope }} · {{ selectedRail()!.currency }}</p>
                  </div>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-text"
                    title="Modifier"
                    aria-label="Modifier"
                    (click)="editRail()"
                  ></button>
                </header>
                <dl>
                  <div><dt>Description</dt><dd>{{ selectedRail()!.description || '—' }}</dd></div>
                  <div><dt>Montants</dt><dd>{{ selectedRail()!.minAmount | number: '1.0-0' }} – {{ selectedRail()!.maxAmount | number: '1.0-0' }}</dd></div>
                  <div><dt>Contraintes</dt>
                    <dd>
                      {{ selectedRail()!.requiresBank ? 'Banque' : '—' }}
                      · {{ selectedRail()!.sameCountryOnly ? 'même pays' : '—' }}
                      · {{ selectedRail()!.sameGroupOnly ? 'même groupe' : '—' }}
                      · {{ selectedRail()!.crossBorderOnly ? 'cross-border' : '—' }}
                    </dd>
                  </div>
                  <div><dt>Champs extra</dt><dd>{{ fieldNames(selectedRail()!.extraFields) }}</dd></div>
                  <div><dt>Actif</dt><dd>{{ selectedRail()!.actif ? 'Oui' : 'Non' }}</dd></div>
                </dl>
              } @else {
                <p class="empty">Sélectionnez un rail.</p>
              }
            </section>
          </div>
        </p-tabPanel>

        <p-tabPanel header="Fournisseurs">
          <div class="layout">
            <aside class="list-panel gw-panel">
              @if (loading()) {
                <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
              } @else {
                <button pButton type="button" label="+ Nouveau fournisseur" class="p-button-text" (click)="startNewProvider()"></button>
                <ul>
                  @for (p of providers(); track p.id) {
                    <li [class.active]="selectedProvider()?.id === p.id" (click)="selectProvider(p)">
                      <strong>{{ p.name }}</strong>
                      <span>{{ p.code }} · {{ p.canEmit ? 'émet' : '' }}{{ p.canEmit && p.canReceive ? ' / ' : '' }}{{ p.canReceive ? 'reçoit' : '' }}</span>
                    </li>
                  }
                </ul>
              }
            </aside>
            <section class="detail-panel gw-panel">
              @if (editingProvider()) {
                <h2>{{ selectedProvider() ? 'Modifier' : 'Nouveau' }} fournisseur</h2>
                <form [formGroup]="providerForm" (ngSubmit)="saveProvider()" class="gw-form-grid">
                  <div class="gw-field">
                    <label>Code</label>
                    <input pInputText formControlName="code" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Nom</label>
                    <input pInputText formControlName="name" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Pays</label>
                    <input pInputText formControlName="countryCode" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Type de participant</label>
                    <p-dropdown formControlName="participantKind" [options]="participantOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Groupe</label>
                    <input pInputText formControlName="groupCode" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Min</label>
                    <input pInputText type="number" formControlName="minAmount" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Max</label>
                    <input pInputText type="number" formControlName="maxAmount" class="w-full" />
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="canEmit" [binary]="true" inputId="canEmit" />
                    <label for="canEmit">Peut émettre</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="canReceive" [binary]="true" inputId="canReceive" />
                    <label for="canReceive">Peut recevoir</label>
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="actif" [binary]="true" inputId="providerActif" />
                    <label for="providerActif">Actif</label>
                  </div>
                  <div class="gw-field full">
                    <label>Champs en tant qu'émetteur (JSON)</label>
                    <textarea pInputTextarea rows="5" formControlName="emitterFieldsJson" class="w-full"></textarea>
                  </div>
                  <div class="gw-field full">
                    <label>Champs en tant que bénéficiaire (JSON)</label>
                    <textarea pInputTextarea rows="5" formControlName="beneficiaryFieldsJson" class="w-full"></textarea>
                  </div>
                  <div class="gw-actions full">
                    <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelProvider()"></button>
                    <button pButton type="submit" label="Enregistrer" [disabled]="busy() || providerForm.invalid"></button>
                  </div>
                </form>
              } @else if (selectedProvider()) {
                <header class="detail-head">
                  <div>
                    <h2>{{ selectedProvider()!.name }}</h2>
                    <p>{{ selectedProvider()!.code }} · {{ selectedProvider()!.countryCode }}</p>
                  </div>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-text"
                    title="Modifier"
                    aria-label="Modifier"
                    (click)="editProvider()"
                  ></button>
                </header>
                <dl>
                  <div><dt>Type</dt><dd>{{ selectedProvider()!.participantKind || 'WALLET' }}{{ selectedProvider()!.groupCode ? ' · ' + selectedProvider()!.groupCode : '' }}</dd></div>
                  <div><dt>Rôles</dt><dd>{{ selectedProvider()!.canEmit ? 'Émetteur' : '—' }} / {{ selectedProvider()!.canReceive ? 'Bénéficiaire' : '—' }}</dd></div>
                  <div><dt>Montants</dt><dd>{{ selectedProvider()!.minAmount | number: '1.0-0' }} – {{ selectedProvider()!.maxAmount | number: '1.0-0' }}</dd></div>
                  <div><dt>Champs émetteur</dt><dd>{{ fieldNames(selectedProvider()!.emitterFields) }}</dd></div>
                  <div><dt>Champs bénéficiaire</dt><dd>{{ fieldNames(selectedProvider()!.beneficiaryFields) }}</dd></div>
                </dl>
              } @else {
                <p class="empty">Sélectionnez un fournisseur.</p>
              }
            </section>
          </div>
        </p-tabPanel>

        <p-tabPanel header="Politiques de frais">
          <div class="layout">
            <aside class="list-panel gw-panel">
              <button pButton type="button" label="+ Nouvelle politique" class="p-button-text" (click)="startNewPolicy()"></button>
              <ul>
                @for (p of feePolicies(); track p.id) {
                  <li [class.active]="selectedPolicy()?.id === p.id" (click)="selectPolicy(p)">
                    <strong>{{ p.code }}</strong>
                    <span>{{ policyLabel(p.policyType) }} · {{ p.feeRuleCode }}{{ p.railCode ? ' · ' + p.railCode : '' }}</span>
                  </li>
                }
              </ul>
            </aside>
            <section class="detail-panel gw-panel">
              @if (editingPolicy()) {
                <h2>{{ selectedPolicy() ? 'Modifier' : 'Nouvelle' }} politique</h2>
                <form [formGroup]="policyForm" (ngSubmit)="savePolicy()" class="gw-form-grid">
                  <div class="gw-field">
                    <label>Code</label>
                    <input pInputText formControlName="code" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Nom</label>
                    <input pInputText formControlName="name" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Type</label>
                    <p-dropdown formControlName="policyType" [options]="policyTypeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Grille de frais</label>
                    <p-dropdown formControlName="feeRuleId" [options]="feeRuleOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Rail (optionnel)</label>
                    <p-dropdown formControlName="railCode" [options]="railCodeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Émetteur (override)</label>
                    <input pInputText formControlName="emitterCode" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Bénéficiaire (override)</label>
                    <input pInputText formControlName="beneficiaryCode" class="w-full" />
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="actif" [binary]="true" inputId="policyActif" />
                    <label for="policyActif">Actif</label>
                  </div>
                  <div class="gw-actions full">
                    <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelPolicy()"></button>
                    <button pButton type="submit" label="Enregistrer" [disabled]="busy() || policyForm.invalid"></button>
                  </div>
                </form>
              } @else if (selectedPolicy()) {
                <header class="detail-head">
                  <div>
                    <h2>{{ selectedPolicy()!.name }}</h2>
                    <p>{{ selectedPolicy()!.code }} · {{ policyLabel(selectedPolicy()!.policyType) }}</p>
                  </div>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-text"
                    title="Modifier"
                    aria-label="Modifier"
                    (click)="editPolicy()"
                  ></button>
                </header>
                <dl>
                  <div><dt>Grille</dt><dd>{{ selectedPolicy()!.feeRuleCode }}</dd></div>
                  <div><dt>Rail</dt><dd>{{ selectedPolicy()!.railCode || 'Tous' }}</dd></div>
                  <div><dt>Paire</dt><dd>{{ selectedPolicy()!.emitterCode || '—' }} → {{ selectedPolicy()!.beneficiaryCode || '—' }}</dd></div>
                </dl>
              } @else {
                <p class="empty">SAME / CROSS couvrent 100 fournisseurs. Les overrides sont l'exception.</p>
              }
            </section>
          </div>
        </p-tabPanel>

        <p-tabPanel header="Grilles de frais">
          <p class="gw-msg tarif-note">
            <i class="pi pi-info-circle"></i>
            Ces grilles sont celles du gateway. Le nouveau moteur de tarification (grilles par tranches, formules,
            simulateur) se configure dans <a routerLink="/admin/tarification/grilles">Tarification</a>.
          </p>
          <div class="layout">
            <aside class="list-panel gw-panel">
              <button pButton type="button" label="+ Nouvelle grille" class="p-button-text" (click)="startNewFeeRule()"></button>
              <ul>
                @for (r of feeRules(); track r.id) {
                  <li [class.active]="selectedFeeRule()?.id === r.id" (click)="selectFeeRule(r)">
                    <strong>{{ r.code }}</strong>
                    <span>{{ r.name }}</span>
                  </li>
                }
              </ul>
            </aside>
            <section class="detail-panel gw-panel">
              @if (editingFeeRule()) {
                <h2>{{ selectedFeeRule() ? 'Modifier' : 'Nouvelle' }} grille</h2>
                <form [formGroup]="feeForm" (ngSubmit)="saveFeeRule()" class="gw-form-grid">
                  <div class="gw-field">
                    <label>Code</label>
                    <input pInputText formControlName="code" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Nom</label>
                    <input pInputText formControlName="name" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Porteur</label>
                    <p-dropdown formControlName="feeBearer" [options]="feeBearerOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                  </div>
                  <div class="gw-field full check-row">
                    <p-checkbox formControlName="actif" [binary]="true" inputId="feeActif" />
                    <label for="feeActif">Actif</label>
                  </div>
                  <div class="lines-head full">
                    <h3>Paliers</h3>
                    <button pButton type="button" label="+ Palier" class="p-button-text" (click)="addFeeLine()"></button>
                  </div>
                  <div formArrayName="lines" class="lines full">
                    @for (line of feeLines.controls; track line; let i = $index) {
                      <div [formGroupName]="i" class="line-row">
                        <div class="gw-field">
                          <label>Type</label>
                          <p-dropdown formControlName="feeType" [options]="feeTypeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
                        </div>
                        <div class="gw-field">
                          <label>Min</label>
                          <input pInputText type="number" formControlName="minAmount" class="w-full" />
                        </div>
                        <div class="gw-field">
                          <label>Max</label>
                          <input pInputText type="number" formControlName="maxAmount" class="w-full" />
                        </div>
                        <div class="gw-field">
                          <label>Valeur</label>
                          <input pInputText type="number" formControlName="value" class="w-full" />
                        </div>
                        <button pButton type="button" icon="pi pi-times" class="p-button-text p-button-danger" (click)="removeFeeLine(i)"></button>
                      </div>
                    }
                  </div>
                  <div class="gw-actions full">
                    <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelFeeRule()"></button>
                    <button pButton type="submit" label="Enregistrer" [disabled]="busy() || feeForm.invalid"></button>
                  </div>
                </form>
              } @else if (selectedFeeRule()) {
                <header class="detail-head">
                  <div><h2>{{ selectedFeeRule()!.name }}</h2><p>{{ selectedFeeRule()!.code }}</p></div>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-text"
                    title="Modifier"
                    aria-label="Modifier"
                    (click)="editFeeRule()"
                  ></button>
                </header>
                <ul class="fee-lines">
                  @for (l of selectedFeeRule()!.lines; track l.ordre) {
                    <li>{{ l.minAmount ?? 0 }} – {{ l.maxAmount ?? '∞' }} : {{ lineLabel(l) }}</li>
                  }
                </ul>
              } @else {
                <p class="empty">Sélectionnez une grille.</p>
              }
            </section>
          </div>
        </p-tabPanel>
      </p-tabView>
    </div>
  `,
  styles: `
    .tarif-note { margin: 0 0 0.5rem; padding: 0.5rem 0.75rem; background: #f0f9ff; border-radius: 8px; }
    .layout { display: grid; grid-template-columns: 280px 1fr; gap: 1rem; padding-top: 0.5rem; }
    @media (max-width: 900px) { .layout { grid-template-columns: 1fr; } }
    .w-full { width: 100%; }
    .full { grid-column: 1 / -1; }
    .check-row { flex-direction: row; align-items: center; gap: 0.5rem; }
    .list-panel ul { list-style: none; margin: 0; padding: 0; }
    .list-panel li { padding: 0.85rem 1rem; border-bottom: 1px solid #e2e8f0; cursor: pointer; display: flex; flex-direction: column; gap: 0.2rem; }
    .list-panel li span { font-size: 0.8rem; color: #64748b; }
    .list-panel li.active, .list-panel li:hover { background: #eff6ff; }
    .detail-head { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; }
    .detail-head p { margin: 0.25rem 0 0; color: #64748b; }
    .lines-head { display: flex; justify-content: space-between; align-items: center; }
    .line-row { display: grid; grid-template-columns: repeat(4, 1fr) auto; gap: 0.5rem; margin-bottom: 0.5rem; align-items: end; }
    dl > div { display: grid; grid-template-columns: 10rem 1fr; gap: 0.5rem; padding: 0.35rem 0; border-bottom: 1px solid #e2e8f0; }
    dl dt { color: #64748b; }
    dl dd { margin: 0; }
    .fee-lines { list-style: none; padding: 0; }
    .empty { color: #64748b; }
  `
})
export class TransfersAdminComponent implements OnInit {
  private readonly api = inject(TransferService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly providers = signal<TransferProvider[]>([]);
  readonly rails = signal<TransferRail[]>([]);
  readonly feeRules = signal<FeeRule[]>([]);
  readonly feePolicies = signal<TransferFeePolicy[]>([]);
  readonly selectedProvider = signal<TransferProvider | null>(null);
  readonly selectedRail = signal<TransferRail | null>(null);
  readonly selectedFeeRule = signal<FeeRule | null>(null);
  readonly selectedPolicy = signal<TransferFeePolicy | null>(null);
  readonly editingProvider = signal(false);
  readonly editingRail = signal(false);
  readonly editingFeeRule = signal(false);
  readonly editingPolicy = signal(false);

  readonly railScopeOptions: { label: string; value: RailScope }[] = [
    { label: 'Interne', value: 'INTERNAL' },
    { label: 'Externe', value: 'EXTERNAL' }
  ];
  readonly participantOptions: { label: string; value: ParticipantKind }[] = [
    { label: 'Portefeuille', value: 'WALLET' },
    { label: 'Banque', value: 'BANK' }
  ];
  readonly policyTypeOptions: { label: string; value: FeePolicyType }[] = [
    { label: 'Même fournisseur', value: 'SAME_PROVIDER' },
    { label: 'Inter-fournisseurs', value: 'CROSS_PROVIDER' },
    { label: 'Override de paire', value: 'PAIR_OVERRIDE' }
  ];
  readonly feeBearerOptions = [
    { label: 'Émetteur', value: 'SENDER' as const },
    { label: 'Bénéficiaire', value: 'RECIPIENT' as const }
  ];
  readonly feeTypeOptions = [
    { label: 'Fixe', value: 'FIXED' as const },
    { label: '%', value: 'PERCENT' as const }
  ];

  get feeRuleOptions(): { label: string; value: string }[] {
    return this.feeRules().map((r) => ({ label: `${r.code} — ${r.name}`, value: r.id }));
  }

  get railCodeOptions(): { label: string; value: string | null }[] {
    return [
      { label: '— Tous les rails —', value: null },
      ...this.rails().map((r) => ({ label: `${r.code} — ${r.name}`, value: r.code }))
    ];
  }

  readonly providerForm = this.fb.group({
    code: this.fb.nonNullable.control('', Validators.required),
    name: this.fb.nonNullable.control('', Validators.required),
    countryCode: this.fb.nonNullable.control('BF'),
    participantKind: this.fb.nonNullable.control<ParticipantKind>('WALLET'),
    groupCode: this.fb.control<string | null>(null),
    canEmit: this.fb.nonNullable.control(true),
    canReceive: this.fb.nonNullable.control(true),
    minAmount: this.fb.control<number | null>(100),
    maxAmount: this.fb.control<number | null>(2000000),
    actif: this.fb.nonNullable.control(true),
    emitterFieldsJson: this.fb.nonNullable.control(this.defaultMsisdnJson()),
    beneficiaryFieldsJson: this.fb.nonNullable.control(this.defaultMsisdnJson())
  });

  readonly railForm = this.fb.group({
    code: this.fb.nonNullable.control('', Validators.required),
    name: this.fb.nonNullable.control('', Validators.required),
    description: this.fb.control<string | null>(null),
    railScope: this.fb.nonNullable.control<RailScope>('INTERNAL', Validators.required),
    currency: this.fb.nonNullable.control('XOF'),
    minAmount: this.fb.control<number | null>(100),
    maxAmount: this.fb.control<number | null>(2000000),
    requiresBank: this.fb.nonNullable.control(false),
    sameCountryOnly: this.fb.nonNullable.control(true),
    sameGroupOnly: this.fb.nonNullable.control(false),
    crossBorderOnly: this.fb.nonNullable.control(false),
    actif: this.fb.nonNullable.control(true),
    extraFieldsJson: this.fb.nonNullable.control('[]')
  });

  readonly policyForm = this.fb.group({
    code: this.fb.nonNullable.control('', Validators.required),
    name: this.fb.nonNullable.control('', Validators.required),
    policyType: this.fb.nonNullable.control<FeePolicyType>('SAME_PROVIDER', Validators.required),
    feeRuleId: this.fb.nonNullable.control('', Validators.required),
    railCode: this.fb.control<string | null>(null),
    emitterCode: this.fb.control<string | null>(null),
    beneficiaryCode: this.fb.control<string | null>(null),
    actif: this.fb.nonNullable.control(true)
  });

  readonly feeForm = this.fb.group({
    code: this.fb.nonNullable.control('', Validators.required),
    name: this.fb.nonNullable.control('', Validators.required),
    currency: this.fb.nonNullable.control('XOF'),
    feeBearer: this.fb.nonNullable.control<'SENDER' | 'RECIPIENT'>('SENDER'),
    actif: this.fb.nonNullable.control(true),
    lines: this.fb.array([])
  });

  get feeLines(): FormArray {
    return this.feeForm.controls.lines as FormArray;
  }

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.api.adminListProviders().subscribe({
      next: (list) => { this.providers.set(list); this.loading.set(false); },
      error: (err: { message?: string }) => { this.error.set(err?.message ?? 'Chargement impossible'); this.loading.set(false); }
    });
    this.api.adminListRails().subscribe({
      next: (list) => this.rails.set(list),
      error: (err: { message?: string }) => this.error.set(err?.message ?? 'Rails indisponibles')
    });
    this.api.adminListFeeRules().subscribe({ next: (list) => this.feeRules.set(list), error: () => {} });
    this.api.adminListFeePolicies().subscribe({ next: (list) => this.feePolicies.set(list), error: () => {} });
  }

  selectRail(r: TransferRail): void { this.selectedRail.set(r); this.editingRail.set(false); }
  startNewRail(): void {
    this.selectedRail.set(null);
    this.editingRail.set(true);
    this.railForm.reset({
      code: '', name: '', description: null, railScope: 'INTERNAL', currency: 'XOF',
      minAmount: 100, maxAmount: 2000000,
      requiresBank: false, sameCountryOnly: true, sameGroupOnly: false, crossBorderOnly: false,
      actif: true, extraFieldsJson: '[]'
    });
  }
  editRail(): void {
    const r = this.selectedRail();
    if (!r) return;
    this.editingRail.set(true);
    this.railForm.reset({
      code: r.code, name: r.name, description: r.description ?? null,
      railScope: r.railScope, currency: r.currency || 'XOF',
      minAmount: r.minAmount, maxAmount: r.maxAmount,
      requiresBank: r.requiresBank, sameCountryOnly: r.sameCountryOnly,
      sameGroupOnly: r.sameGroupOnly, crossBorderOnly: r.crossBorderOnly,
      actif: r.actif, extraFieldsJson: this.stringifyFields(r.extraFields)
    });
  }
  cancelRail(): void { this.editingRail.set(false); }
  saveRail(): void {
    if (this.railForm.invalid) return;
    const v = this.railForm.getRawValue();
    let extraFields: TransferField[];
    try {
      extraFields = this.parseFields(v.extraFieldsJson ?? '[]');
    } catch {
      this.error.set('JSON des champs supplémentaires invalide');
      return;
    }
    this.busy.set(true);
    this.api.adminSaveRail({
      code: v.code!, name: v.name!, description: v.description,
      railScope: v.railScope!, currency: v.currency!,
      minAmount: v.minAmount, maxAmount: v.maxAmount,
      requiresBank: !!v.requiresBank, sameCountryOnly: !!v.sameCountryOnly,
      sameGroupOnly: !!v.sameGroupOnly, crossBorderOnly: !!v.crossBorderOnly,
      extraFields, actif: !!v.actif, ordre: this.rails().length + 1
    }, this.selectedRail()?.id).subscribe({
      next: (saved) => { this.busy.set(false); this.editingRail.set(false); this.reload(); this.selectRail(saved); },
      error: (err: { message?: string }) => { this.busy.set(false); this.error.set(err?.message ?? 'Enregistrement impossible'); }
    });
  }

  selectProvider(p: TransferProvider): void { this.selectedProvider.set(p); this.editingProvider.set(false); }
  startNewProvider(): void {
    this.selectedProvider.set(null);
    this.editingProvider.set(true);
    this.providerForm.reset({
      code: '', name: '', countryCode: 'BF', participantKind: 'WALLET', groupCode: null,
      canEmit: true, canReceive: true,
      minAmount: 100, maxAmount: 2000000, actif: true,
      emitterFieldsJson: this.defaultMsisdnJson(), beneficiaryFieldsJson: this.defaultMsisdnJson()
    });
  }
  editProvider(): void {
    const p = this.selectedProvider();
    if (!p) return;
    this.editingProvider.set(true);
    this.providerForm.reset({
      code: p.code, name: p.name, countryCode: p.countryCode,
      participantKind: p.participantKind ?? 'WALLET', groupCode: p.groupCode ?? null,
      canEmit: p.canEmit, canReceive: p.canReceive,
      minAmount: p.minAmount, maxAmount: p.maxAmount, actif: p.actif,
      emitterFieldsJson: this.stringifyFields(p.emitterFields),
      beneficiaryFieldsJson: this.stringifyFields(p.beneficiaryFields)
    });
  }
  cancelProvider(): void { this.editingProvider.set(false); }
  saveProvider(): void {
    if (this.providerForm.invalid) return;
    const v = this.providerForm.getRawValue();
    let emitterFields: TransferField[];
    let beneficiaryFields: TransferField[];
    try {
      emitterFields = this.parseFields(v.emitterFieldsJson ?? '[]');
      beneficiaryFields = this.parseFields(v.beneficiaryFieldsJson ?? '[]');
    } catch {
      this.error.set('JSON des champs invalide');
      return;
    }
    this.busy.set(true);
    this.api.adminSaveProvider({
      code: v.code!, name: v.name!, countryCode: v.countryCode!,
      participantKind: v.participantKind!, groupCode: v.groupCode || null,
      canEmit: !!v.canEmit, canReceive: !!v.canReceive,
      minAmount: v.minAmount, maxAmount: v.maxAmount, actif: !!v.actif,
      emitterFields, beneficiaryFields, ordreAffichage: this.providers().length + 1,
      providerId: this.selectedProvider()?.providerId ?? null
    }, this.selectedProvider()?.id).subscribe({
      next: (saved) => { this.busy.set(false); this.editingProvider.set(false); this.reload(); this.selectProvider(saved); },
      error: (err: { message?: string }) => { this.busy.set(false); this.error.set(err?.message ?? 'Enregistrement impossible'); }
    });
  }

  selectPolicy(p: TransferFeePolicy): void { this.selectedPolicy.set(p); this.editingPolicy.set(false); }
  startNewPolicy(): void {
    this.selectedPolicy.set(null);
    this.editingPolicy.set(true);
    this.policyForm.reset({
      code: '', name: '', policyType: 'SAME_PROVIDER',
      feeRuleId: this.feeRules()[0]?.id || '', railCode: null,
      emitterCode: null, beneficiaryCode: null, actif: true
    });
  }
  editPolicy(): void {
    const p = this.selectedPolicy();
    if (!p) return;
    this.editingPolicy.set(true);
    this.policyForm.reset({
      code: p.code, name: p.name, policyType: p.policyType, feeRuleId: p.feeRuleId,
      railCode: p.railCode ?? null,
      emitterCode: p.emitterCode, beneficiaryCode: p.beneficiaryCode, actif: p.actif
    });
  }
  cancelPolicy(): void { this.editingPolicy.set(false); }
  savePolicy(): void {
    if (this.policyForm.invalid) return;
    const v = this.policyForm.getRawValue();
    this.busy.set(true);
    this.api.adminSaveFeePolicy({
      code: v.code!, name: v.name!, policyType: v.policyType!,
      countryCode: 'BF', currency: 'XOF',
      emitterCode: v.emitterCode || null, beneficiaryCode: v.beneficiaryCode || null,
      railCode: v.railCode || null,
      feeRuleId: v.feeRuleId!, actif: !!v.actif, ordre: 0
    }, this.selectedPolicy()?.id).subscribe({
      next: (saved) => {
        this.busy.set(false); this.editingPolicy.set(false);
        this.api.adminListFeePolicies().subscribe({ next: (list) => this.feePolicies.set(list) });
        this.selectPolicy(saved);
      },
      error: (err: { message?: string }) => { this.busy.set(false); this.error.set(err?.message ?? 'Enregistrement impossible'); }
    });
  }

  selectFeeRule(r: FeeRule): void { this.selectedFeeRule.set(r); this.editingFeeRule.set(false); }
  startNewFeeRule(): void {
    this.selectedFeeRule.set(null);
    this.editingFeeRule.set(true);
    this.feeForm.reset({ code: '', name: '', currency: 'XOF', feeBearer: 'SENDER', actif: true });
    this.feeLines.clear();
    this.addFeeLine();
  }
  editFeeRule(): void {
    const r = this.selectedFeeRule();
    if (!r) return;
    this.editingFeeRule.set(true);
    this.feeForm.reset({ code: r.code, name: r.name, currency: r.currency, feeBearer: r.feeBearer ?? 'SENDER', actif: r.actif });
    this.feeLines.clear();
    for (const line of r.lines) this.feeLines.push(this.buildFeeLineGroup(line));
  }
  cancelFeeRule(): void { this.editingFeeRule.set(false); }
  saveFeeRule(): void {
    if (this.feeForm.invalid) return;
    const v = this.feeForm.getRawValue();
    const rawLines = (v.lines ?? []) as FeeRuleLine[];
    const lines = rawLines.map((l, i) => ({
      feeType: l.feeType, minAmount: l.minAmount, maxAmount: l.maxAmount, value: l.value,
      fixedAmount: l.fixedAmount ?? (l.feeType === 'FIXED' ? l.value : null),
      percentValue: l.percentValue ?? (l.feeType === 'PERCENT' ? l.value : null),
      minFee: l.minFee, maxFee: l.maxFee, ordre: i
    }));
    this.busy.set(true);
    this.api.adminSaveFeeRule({
      code: v.code!, name: v.name!, currency: v.currency!, feeBearer: v.feeBearer!, actif: !!v.actif, lines
    }, this.selectedFeeRule()?.id).subscribe({
      next: (saved) => {
        this.busy.set(false); this.editingFeeRule.set(false);
        this.api.adminListFeeRules().subscribe({ next: (list) => this.feeRules.set(list) });
        this.selectFeeRule(saved);
      },
      error: (err: { message?: string }) => { this.busy.set(false); this.error.set(err?.message ?? 'Enregistrement impossible'); }
    });
  }

  addFeeLine(): void {
    this.feeLines.push(this.buildFeeLineGroup({
      feeType: 'FIXED', minAmount: 0, maxAmount: null, value: 100, ordre: this.feeLines.length
    }));
  }
  removeFeeLine(i: number): void { this.feeLines.removeAt(i); }

  policyLabel(t: FeePolicyType): string {
    switch (t) {
      case 'SAME_PROVIDER': return 'Même fournisseur';
      case 'CROSS_PROVIDER': return 'Inter-fournisseurs';
      case 'PAIR_OVERRIDE': return 'Override paire';
    }
  }

  fieldNames(fields?: TransferField[]): string {
    return fields?.map((f) => f.name).join(', ') || '—';
  }

  lineLabel(l: FeeRuleLine): string {
    if (l.feeType === 'PERCENT') return `${l.value}%`;
    return `fixe ${l.value}`;
  }

  private buildFeeLineGroup(line: FeeRuleLine) {
    return this.fb.group({
      feeType: this.fb.nonNullable.control<'FIXED' | 'PERCENT'>(line.feeType),
      minAmount: this.fb.control<number | null>(line.minAmount),
      maxAmount: this.fb.control<number | null>(line.maxAmount),
      value: this.fb.nonNullable.control(line.value, Validators.required),
      fixedAmount: this.fb.control<number | null>(line.fixedAmount ?? null),
      percentValue: this.fb.control<number | null>(line.percentValue ?? null),
      minFee: this.fb.control<number | null>(line.minFee ?? null),
      maxFee: this.fb.control<number | null>(line.maxFee ?? null)
    });
  }

  private defaultMsisdnJson(): string {
    return JSON.stringify([{
      name: 'msisdn', label: 'Numéro mobile', type: 'tel', required: true,
      placeholder: '22670000000', pattern: '^226\\d{8}$'
    }], null, 2);
  }

  private stringifyFields(fields?: TransferField[]): string {
    return JSON.stringify(fields ?? [], null, 2);
  }

  private parseFields(raw: string): TransferField[] {
    const parsed = JSON.parse(raw || '[]');
    if (!Array.isArray(parsed)) throw new Error('Expected array');
    return parsed as TransferField[];
  }
}
