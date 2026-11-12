import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { Environment, FseApi, Operation, Provider } from '../../core/model/api.models';

@Component({
  selector: 'app-provider-operations',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-page-header">
      <div>
        <h2>Méthodes</h2>
        <p class="subtitle">Comme dans Omnicanal : TypeMethode + codeMethode + nomMethode (path) + paramètres</p>
      </div>
      <div class="header-actions">
        @if (canSeedBundle()) {
          <button
            pButton
            type="button"
            class="p-button-outlined"
            label="Charger le catalogue Coris"
            icon="pi pi-download"
            [loading]="savingOmni()"
            (click)="registerOmnicanalBundle()"
          ></button>
        }
        <button
          pButton
          type="button"
          label="Ajouter une méthode"
          icon="pi pi-plus"
          (click)="toggleForm()"
        ></button>
      </div>
    </div>

    @if (showForm()) {
      <div class="gw-panel">
        <h3>Nouvelle méthode (format Omnicanal)</h3>
        <form [formGroup]="form" (ngSubmit)="saveMethod()" class="gw-form-grid">
          <div class="gw-field">
            <label>Type méthode *</label>
            <p-dropdown
              formControlName="typeMethode"
              [options]="typeMethodeSelectOptions()"
              optionLabel="label"
              optionValue="value"
              [filter]="true"
              [editable]="true"
              placeholder="Sélectionner ou saisir…"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label>Code méthode</label>
            <input pInputText formControlName="codeMethode" class="w-full" placeholder="ex. 000703" />
          </div>
          <div class="gw-field full">
            <label>Nom méthode (path URL) *</label>
            <input pInputText formControlName="nomMethode" class="w-full" placeholder="ex. api/gateway/solde" />
          </div>
          <div class="gw-field">
            <label>Api *</label>
            <p-dropdown
              formControlName="fseApiId"
              [options]="fseApiOptions()"
              optionLabel="label"
              optionValue="value"
              placeholder="Choisir une Api"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label>Type appel *</label>
            <p-dropdown formControlName="typeAppel" [options]="httpMethods" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Type réponse</label>
            <p-dropdown formControlName="typeReponse" [options]="responseTypes" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
          </div>

          <div class="gw-field full">
            <div class="params-header">
              <label>Paramètres (ParametreMethode)</label>
              <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-plus" label="Paramètre" (click)="addParam()"></button>
            </div>
            <div formArrayName="parametres" class="params-list">
              @for (ctrl of parametres.controls; track ctrl; let i = $index) {
                <div [formGroupName]="i" class="param-row">
                  <input pInputText formControlName="codeParametre" placeholder="codeParametre (interne)" />
                  <input pInputText formControlName="nomExterne" placeholder="nomExterne (clé JSON)" />
                  <input pInputText formControlName="typeParametre" placeholder="STRING" />
                  <button pButton type="button" class="p-button-rounded p-button-text p-button-danger" icon="pi pi-trash" (click)="removeParam(i)"></button>
                </div>
              }
            </div>
            <small class="hint">Mapping généré : &#123;"nomExterne":"$&#123;codeParametre&#125;"&#125; — comme Omnicanal</small>
          </div>

          <div class="gw-actions full">
            <button
              pButton
              type="submit"
              label="Enregistrer la méthode"
              icon="pi pi-save"
              [disabled]="form.invalid || saving()"
              [loading]="saving()"
            ></button>
            <button pButton type="button" class="p-button-text" label="Annuler" (click)="toggleForm()"></button>
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
              <th>Type / code</th>
              <th>Nom / path</th>
              <th>Description</th>
              <th>Actif</th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-o>
            <tr>
              <td><code>{{ o.code }}</code></td>
              <td>{{ o.nom }}</td>
              <td>{{ o.description ?? '—' }}</td>
              <td>{{ o.actif ? 'Oui' : 'Non' }}</td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="4">Aucune méthode — cliquez sur « Ajouter une méthode »</td></tr>
          </ng-template>
        </p-table>
      </div>
    }
    @if (msg()) {
      <p class="gw-msg" [class.error]="isError()">{{ msg() }}</p>
    }
  `,
  styles: `
    .subtitle { margin: 0.15rem 0 0; color: var(--text-color-secondary, #64748b); font-size: 0.85rem; }
    .header-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; }
    h3 { margin: 0 0 0.75rem; font-size: 1rem; }
    code { font-size: 0.85rem; }
    .params-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .params-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .param-row { display: grid; grid-template-columns: 1fr 1fr 7rem auto; gap: 0.5rem; align-items: center; }
    .hint { display: block; margin-top: 0.35rem; color: var(--text-color-secondary, #64748b); font-size: 0.8rem; }
    @media (max-width: 720px) {
      .param-row { grid-template-columns: 1fr; }
    }
  `
})
export class ProviderOperationsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly httpMethods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
  readonly responseTypes = ['JSON', 'XML'];

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly savingOmni = signal(false);
  readonly showForm = signal(false);
  readonly items = signal<Operation[]>([]);
  readonly fseApis = signal<FseApi[]>([]);
  readonly environments = signal<Environment[]>([]);
  readonly provider = signal<Provider | null>(null);
  readonly typeMethodeOptions = signal<string[]>([]);
  readonly typeMethodeSelectOptions = computed(() => {
    const catalog = this.typeMethodeOptions();
    const fromOps = this.items().map((o) => o.code);
    const current = this.form.controls.typeMethode.value?.trim();
    const all = [...catalog, ...fromOps];
    if (current) {
      all.push(current);
    }
    return [...new Set(all.filter((v) => !!v))]
      .sort((a, b) => a.localeCompare(b))
      .map((value) => ({ label: value, value }));
  });
  readonly msg = signal<string | null>(null);
  readonly isError = signal(false);

  readonly form = this.fb.nonNullable.group({
    fseApiId: [null as string | null, Validators.required],
    typeMethode: ['', Validators.required],
    codeMethode: [''],
    nomMethode: ['', Validators.required],
    typeAppel: ['POST', Validators.required],
    typeReponse: ['JSON'],
    actif: [true],
    parametres: this.fb.array([])
  });

  get parametres(): FormArray {
    return this.form.get('parametres') as FormArray;
  }

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    forkJoin({
      provider: this.api.get(this.providerId),
      types: this.api.listOmnicanalTypeMethodes(),
      fseApis: this.api.listFseApis(this.providerId),
      envs: this.api.listEnvironments(this.providerId),
      ops: this.api.listOperations(this.providerId)
    }).subscribe({
      next: ({ provider, types, fseApis, envs, ops }) => {
        this.provider.set(provider);
        this.typeMethodeOptions.set(types);
        this.fseApis.set(fseApis);
        this.environments.set(envs);
        this.items.set(ops);
        this.loading.set(false);
        if (fseApis.length > 0) {
          this.form.patchValue({ fseApiId: fseApis[0].id });
        }
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur de chargement');
        this.isError.set(true);
        this.loading.set(false);
      }
    });
  }

  fseApiOptions(): Array<{ label: string; value: string }> {
    return this.fseApis().map((a) => ({
      label: `${a.code} — ${a.nom}`,
      value: a.id
    }));
  }

  canSeedBundle(): boolean {
    const p = this.provider();
    if (!p) return false;
    return p.codeFse === '0011' || p.codeFse === '0004'
      || p.code === 'CORIS_MONEY' || p.code === 'CORIS_BANK';
  }

  toggleForm(): void {
    const next = !this.showForm();
    this.showForm.set(next);
    if (next && this.parametres.length === 0) {
      this.addParam();
    }
  }

  addParam(): void {
    this.parametres.push(
      this.fb.nonNullable.group({
        codeParametre: ['', Validators.required],
        nomExterne: ['', Validators.required],
        typeParametre: ['STRING'],
        categorie: ['USER']
      })
    );
  }

  removeParam(index: number): void {
    this.parametres.removeAt(index);
  }

  saveMethod(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.msg.set(null);
    const v = this.form.getRawValue();
    const parametres = (v.parametres as Array<{
      codeParametre: string;
      nomExterne: string;
      typeParametre: string;
      categorie: string;
    }>)
      .filter((p) => p.codeParametre?.trim() && p.nomExterne?.trim())
      .map((p) => ({
        codeParametre: p.codeParametre.trim(),
        nomExterne: p.nomExterne.trim(),
        typeParametre: p.typeParametre || 'STRING',
        categorie: p.categorie || 'USER'
      }));

    this.api
      .createOmnicanalMethod(this.providerId, {
        fseApiId: v.fseApiId!,
        typeMethode: v.typeMethode,
        codeMethode: v.codeMethode || null,
        nomMethode: v.nomMethode,
        typeAppel: v.typeAppel,
        typeReponse: v.typeReponse,
        actif: v.actif,
        idempotent: false,
        parametres
      })
      .subscribe({
        next: (res) => {
          this.saving.set(false);
          this.isError.set(false);
          this.msg.set(
            `Méthode ${res.operationCode} enregistrée sur Api ${res.fseApiCode} → ${res.httpMethod} ${res.path}`
          );
          this.showForm.set(false);
          this.form.reset({
            fseApiId: this.fseApis()[0]?.id ?? null,
            typeMethode: '',
            codeMethode: '',
            nomMethode: '',
            typeAppel: 'POST',
            typeReponse: 'JSON',
            actif: true
          });
          this.parametres.clear();
          this.reload();
        },
        error: (err: { message?: string }) => {
          this.saving.set(false);
          this.isError.set(true);
          this.msg.set(err?.message ?? 'Enregistrement impossible');
        }
      });
  }

  registerOmnicanalBundle(): void {
    this.savingOmni.set(true);
    this.api.seedOmnicanalConnectivity(this.providerId).subscribe({
      next: (res) => {
        this.savingOmni.set(false);
        this.isError.set(false);
        this.msg.set(`Catalogue Coris chargé : ${res.operationsUpserted} méthode(s) sur ${res.environmentCode}`);
        this.reload();
      },
      error: (err: { message?: string }) => {
        this.savingOmni.set(false);
        this.isError.set(true);
        this.msg.set(err?.message ?? 'Chargement catalogue impossible');
      }
    });
  }

  reload(): void {
    this.api.listOperations(this.providerId).subscribe({
      next: (list) => this.items.set(list),
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.isError.set(true);
      }
    });
  }
}
