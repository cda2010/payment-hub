import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { OrchestrationService } from '../../core/service/orchestration.service';
import { ProviderService } from '../../core/service/provider.service';
import {
  CompteFse,
  CompteFseRequest,
  RelationFseCorridor,
  SERVICE_TYPE_CODES,
  ServiceTypeCode,
  TypeCompte,
} from '../../core/model/api.models';

const SERVICE_TYPES = SERVICE_TYPE_CODES;

@Component({
  selector: 'app-comptes-fse-admin',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    TooltipModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Comptes FSE</h1>
          <p class="subtitle">
            Catalogue des comptes (LORO, MIROIR, compensation…) — saisie libre par FSE teneur ;
            rattachement corridor optionnel
            @if (!loading()) {
              · <strong>{{ filtered().length }}</strong> / {{ comptes().length }}
            }
          </p>
        </div>
        <div class="header-actions">
          <button
            pButton
            type="button"
            class="p-button-outlined"
            label="Actualiser"
            icon="pi pi-refresh"
            [disabled]="loading()"
            (click)="reload()"
          ></button>
          <button
            pButton
            type="button"
            label="Nouveau compte"
            icon="pi pi-plus"
            (click)="openCreate()"
          ></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }

      <div class="gw-panel filters">
        <input
          pInputText
          class="filter-q"
          placeholder="N° compte / type / FSE…"
          [ngModel]="query()"
          (ngModelChange)="query.set($event)"
        />
        <p-dropdown
          [options]="typeFilterOptions()"
          [ngModel]="filterType()"
          (ngModelChange)="filterType.set($event)"
          placeholder="Type"
          [showClear]="true"
          styleClass="filter-dd"
        />
        <p-dropdown
          [options]="fseFilterOptions()"
          [ngModel]="filterTeneur()"
          (ngModelChange)="filterTeneur.set($event)"
          placeholder="FSE teneur"
          [showClear]="true"
          styleClass="filter-dd"
        />
        <p-dropdown
          [options]="serviceTypeOptions"
          optionLabel="label"
          optionValue="value"
          [ngModel]="filterService()"
          (ngModelChange)="filterService.set($event)"
          placeholder="Service"
          [showClear]="true"
          styleClass="filter-dd"
        />
      </div>

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <p-table
            [value]="filtered()"
            [paginator]="true"
            [rows]="25"
            [rowsPerPageOptions]="[10, 25, 50, 100]"
            [rowHover]="true"
            dataKey="id"
            styleClass="p-datatable-sm"
          >
            <ng-template pTemplate="header">
              <tr>
                <th>N° compte</th>
                <th>Banque / Agence</th>
                <th>Type</th>
                <th>Teneur</th>
                <th>Titulaire</th>
                <th>Devise</th>
                <th>Corridor</th>
                <th style="width: 8rem"></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-c>
              <tr>
                <td>
                  <code>{{ c.numeroCompte }}</code>
                  @if (c.cle) {
                    <span class="hint">clé {{ c.cle }}</span>
                  }
                </td>
                <td>
                  <code>{{ c.codeBanque }}</code>
                  <span class="hint">{{ c.codeAgence || c.agenceCode || '—' }}</span>
                </td>
                <td>
                  <p-tag [value]="c.typeCompteCode" [severity]="typeSeverity(c.typeCompteCode)" />
                  @if (c.typeCompteLibelle) {
                    <span class="hint">{{ c.typeCompteLibelle }}</span>
                  }
                </td>
                <td>
                  <p-tag [value]="c.fseTeneur" severity="secondary" />
                </td>
                <td><code>{{ c.fseTitulaire }}</code></td>
                <td>{{ c.devise || '—' }}</td>
                <td>
                  @if (c.fseSource && c.fseDestination) {
                    <span class="corridor">
                      <code>{{ c.fseSource }}</code>
                      →
                      <code>{{ c.fseDestination }}</code>
                    </span>
                  } @else {
                    <span class="muted">—</span>
                  }
                </td>
                <td class="actions">
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-text p-button-sm"
                    pTooltip="Modifier"
                    (click)="openEdit(c)"
                  ></button>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-trash"
                    class="p-button-text p-button-sm p-button-danger"
                    pTooltip="Supprimer"
                    (click)="confirmDelete(c)"
                  ></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="8">
                  Aucun compte
                  @if (comptes().length) {
                    pour ce filtre
                  } @else {
                    — créez un compte ou importez depuis Omnicanal.
                  }
                </td>
              </tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <p-dialog
      [(visible)]="dialogVisible"
      [header]="editId ? 'Modifier le compte' : 'Nouveau compte'"
      [modal]="true"
      [style]="{ width: 'min(72rem, 96vw)' }"
      [contentStyle]="{ 'max-height': 'min(80vh, 52rem)', overflow: 'auto' }"
      [draggable]="false"
      styleClass="compte-fse-dialog"
    >
      <form [formGroup]="form" class="dialog-form" (ngSubmit)="save()">
        <div class="form-grid">
          <label>
            Type de compte
            <p-dropdown
              formControlName="typeCompteCode"
              [options]="typeOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="CPTELORO…"
              [filter]="true"
              styleClass="w-full"
            />
          </label>
          <label>
            Numéro de compte
            <input pInputText formControlName="numeroCompte" class="w-full" maxlength="20" />
          </label>
          <label>
            Clé
            <input pInputText formControlName="cle" class="w-full" maxlength="5" />
          </label>
          <label>
            Code banque
            <input pInputText formControlName="codeBanque" class="w-full" maxlength="20" />
          </label>
          <label>
            Code agence
            <input pInputText formControlName="codeAgence" class="w-full" maxlength="20" />
          </label>
          <label>
            Devise
            <input pInputText formControlName="devise" class="w-full" maxlength="10" />
          </label>
          <label>
            FSE teneur
            <p-dropdown
              formControlName="fseTeneur"
              [options]="fseOptions"
              [editable]="true"
              placeholder="Code FSE"
              [filter]="true"
              styleClass="w-full"
            />
          </label>
          <label>
            FSE titulaire
            <p-dropdown
              formControlName="fseTitulaire"
              [options]="fseOptions"
              [editable]="true"
              placeholder="Code FSE"
              [filter]="true"
              styleClass="w-full"
            />
          </label>
          <label>
            Agence (code)
            <input pInputText formControlName="agenceCode" class="w-full" maxlength="20" />
          </label>
          <label>
            Code UO titulaire
            <input pInputText formControlName="codeUoTitulaire" class="w-full" maxlength="20" />
          </label>
          <label>
            Service (optionnel)
            <p-dropdown
              formControlName="serviceType"
              [options]="serviceTypeOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="Aucun / hérité"
              [showClear]="true"
              styleClass="w-full"
            />
          </label>
          <label>
            Corridor (optionnel)
            <p-dropdown
              formControlName="relationId"
              [options]="relationOptions()"
              optionLabel="label"
              optionValue="value"
              placeholder="Sans corridor"
              [showClear]="true"
              [filter]="true"
              styleClass="w-full"
              (onChange)="onRelationPicked()"
            />
          </label>
          <label class="span-all">
            Libellé
            <input pInputText formControlName="libelleCompte" class="w-full" maxlength="200" />
          </label>
        </div>
        <div class="dialog-actions">
          <button
            pButton
            type="button"
            class="p-button-text"
            label="Annuler"
            (click)="dialogVisible = false"
          ></button>
          <button
            pButton
            type="submit"
            [label]="editId ? 'Enregistrer' : 'Créer'"
            [loading]="saving()"
            [disabled]="form.invalid || saving()"
          ></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .header-actions {
      display: flex;
      align-items: center;
      gap: 0.65rem;
      flex-wrap: wrap;
    }
    .link-muted {
      font-size: 0.85rem;
      color: var(--text-color-secondary, #64748b);
      text-decoration: none;
    }
    .link-muted:hover {
      text-decoration: underline;
    }
    .filters {
      display: flex;
      flex-wrap: wrap;
      gap: 0.65rem;
      align-items: center;
      margin-bottom: 1rem;
    }
    .filter-q {
      min-width: 14rem;
      flex: 1;
    }
    :host ::ng-deep .filter-dd {
      min-width: 10rem;
    }
    .actions {
      white-space: nowrap;
    }
    .hint {
      display: block;
      font-size: 0.75rem;
      color: var(--text-color-secondary, #64748b);
      margin-top: 0.15rem;
    }
    .corridor {
      font-size: 0.85rem;
    }
    .svc {
      font-size: 0.8rem;
    }
    .muted {
      color: var(--text-color-secondary, #94a3b8);
    }
    .dialog-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
      padding: 0.35rem 0.15rem 0.15rem;
      min-height: 22rem;
    }
    .form-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 1rem 1.25rem;
    }
    .form-grid label {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      font-size: 0.9rem;
      font-weight: 500;
    }
    .form-grid .span-all {
      grid-column: 1 / -1;
    }
    .form-grid .span-2 {
      grid-column: span 2;
    }
    .dialog-form label {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      font-size: 0.9rem;
      font-weight: 500;
    }
    .w-full {
      width: 100%;
    }
    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.65rem;
      margin-top: 0.75rem;
      padding-top: 0.85rem;
      border-top: 1px solid var(--surface-border, #e2e8f0);
    }
    :host ::ng-deep .compte-fse-dialog .p-dialog-header {
      padding: 1.15rem 1.35rem;
      font-size: 1.1rem;
    }
    :host ::ng-deep .compte-fse-dialog .p-dialog-content {
      padding: 1.15rem 1.35rem 1.35rem;
    }
    :host ::ng-deep .compte-fse-dialog .p-inputtext,
    :host ::ng-deep .compte-fse-dialog .p-dropdown {
      min-height: 2.65rem;
    }
    @media (max-width: 900px) {
      .form-grid {
        grid-template-columns: 1fr 1fr;
      }
    }
    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
      .form-grid .span-2 {
        grid-column: 1 / -1;
      }
    }
  `,
})
export class ComptesFseAdminComponent implements OnInit {
  private readonly orch = inject(OrchestrationService);
  private readonly providers = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly comptes = signal<CompteFse[]>([]);
  readonly corridors = signal<RelationFseCorridor[]>([]);
  readonly types = signal<TypeCompte[]>([]);
  readonly query = signal('');
  readonly filterType = signal<string | null>(null);
  readonly filterTeneur = signal<string | null>(null);
  readonly filterService = signal<ServiceTypeCode | null>(null);

  private readonly fseNames = signal<Record<string, string>>({});

  dialogVisible = false;
  editId: string | null = null;
  fseOptions: string[] = [];
  typeOptions: { label: string; value: string }[] = [];
  serviceTypeOptions = SERVICE_TYPES.map((v) => ({ label: v, value: v }));

  readonly form = this.fb.nonNullable.group({
    relationId: this.fb.control<string | null>(null),
    typeCompteCode: ['', Validators.required],
    numeroCompte: ['', Validators.required],
    codeBanque: ['00000', Validators.required],
    codeAgence: [''],
    cle: [''],
    devise: ['XOF'],
    fseTeneur: ['', Validators.required],
    fseTitulaire: ['', Validators.required],
    agenceCode: ['01001'],
    codeUoTitulaire: [''],
    libelleCompte: [''],
    serviceType: this.fb.control<ServiceTypeCode | null>(null),
  });

  readonly typeFilterOptions = computed(() => {
    const set = new Set<string>();
    for (const c of this.comptes()) {
      if (c.typeCompteCode) {
        set.add(c.typeCompteCode);
      }
    }
    return [...set].sort();
  });

  readonly fseFilterOptions = computed(() => {
    const set = new Set<string>();
    for (const c of this.comptes()) {
      if (c.fseTeneur) {
        set.add(c.fseTeneur);
      }
    }
    return [...set].sort();
  });

  readonly relationOptions = computed(() =>
    this.corridors().map((r) => ({
      label: `${r.fseSource} → ${r.fseDestination} (${r.serviceType})`,
      value: r.id,
      source: r.fseSource,
      dest: r.fseDestination,
    }))
  );

  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const type = this.filterType();
    const teneur = this.filterTeneur();
    const svc = this.filterService();
    return this.comptes().filter((c) => {
      if (type && c.typeCompteCode !== type) {
        return false;
      }
      if (teneur && c.fseTeneur !== teneur) {
        return false;
      }
      const effectiveSvc = c.serviceType || c.relationServiceType;
      if (svc && effectiveSvc !== svc) {
        return false;
      }
      if (!q) {
        return true;
      }
      const blob = [
        c.numeroCompte,
        c.typeCompteCode,
        c.typeCompteLibelle,
        c.fseTeneur,
        c.fseSource,
        c.fseDestination,
        c.serviceType,
        this.fseNames()[c.fseTeneur] || '',
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return blob.includes(q);
    });
  });

  ngOnInit(): void {
    this.reload();
    this.providers.list().subscribe({
      next: (list) => {
        const map: Record<string, string> = {};
        const codes: string[] = [];
        for (const p of list) {
          const code = (p.codeFse || '').trim();
          if (!code) {
            continue;
          }
          codes.push(code);
          map[code] = p.libelle || p.code;
        }
        this.fseNames.set(map);
        this.fseOptions = [...new Set(codes)].sort();
      },
    });

    const prefillRelation = this.route.snapshot.queryParamMap.get('relationId');
    const prefillType = this.route.snapshot.queryParamMap.get('type');
    if (prefillRelation) {
      queueMicrotask(() => {
        this.openCreate(prefillRelation, prefillType);
      });
    }
  }

  fseLabel(code: string): string | null {
    const name = this.fseNames()[code];
    return name && name.trim() ? name : null;
  }

  typeSeverity(code: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast' {
    switch (code) {
      case 'CPTELORO':
        return 'info';
      case 'CPTEMIROIR':
        return 'success';
      case 'CPTECOMPENSE':
        return 'warning';
      default:
        return 'secondary';
    }
  }

  onRelationPicked(): void {
    const relId = this.form.controls.relationId.value;
    const rel = this.corridors().find((r) => r.id === relId);
    if (!rel) {
      return;
    }
    const type = this.form.controls.typeCompteCode.value;
    if (!this.form.controls.fseTeneur.value) {
      if (type === 'CPTEMIROIR') {
        this.form.controls.fseTeneur.setValue(rel.fseDestination);
      } else {
        this.form.controls.fseTeneur.setValue(rel.fseSource);
      }
    }
    if (!this.form.controls.fseTitulaire.value) {
      this.form.controls.fseTitulaire.setValue(this.form.controls.fseTeneur.value);
    }
  }

  openCreate(relationId?: string | null, typeCode?: string | null): void {
    this.editId = null;
    this.form.reset({
      relationId: relationId || null,
      typeCompteCode: typeCode || 'CPTELORO',
      numeroCompte: '',
      codeBanque: '00000',
      codeAgence: '',
      cle: '',
      devise: 'XOF',
      fseTeneur: '',
      fseTitulaire: '',
      agenceCode: '01001',
      codeUoTitulaire: '',
      libelleCompte: '',
      serviceType: null,
    });
    if (relationId) {
      this.onRelationPicked();
    }
    this.dialogVisible = true;
  }

  openEdit(c: CompteFse): void {
    this.editId = c.id;
    this.form.reset({
      relationId: c.relationId || null,
      typeCompteCode: c.typeCompteCode,
      numeroCompte: c.numeroCompte,
      codeBanque: c.codeBanque || '00000',
      codeAgence: c.codeAgence || '',
      cle: c.cle || '',
      devise: c.devise || '',
      fseTeneur: c.fseTeneur,
      fseTitulaire: c.fseTitulaire || c.fseTeneur,
      agenceCode: c.agenceCode || '01001',
      codeUoTitulaire: c.codeUoTitulaire || '',
      libelleCompte: c.libelleCompte || '',
      serviceType: c.serviceType,
    });
    this.dialogVisible = true;
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const body: CompteFseRequest = {
      numeroCompte: v.numeroCompte.trim(),
      typeCompteCode: v.typeCompteCode.trim(),
      fseTeneur: v.fseTeneur.trim(),
      fseTitulaire: (v.fseTitulaire || v.fseTeneur).trim(),
      codeBanque: (v.codeBanque || '00000').trim(),
      codeAgence: (v.codeAgence || '').trim(),
      cle: v.cle?.trim() || null,
      devise: v.devise?.trim() || null,
      libelleCompte: v.libelleCompte || '',
      codeUoTitulaire: v.codeUoTitulaire?.trim() || null,
      agenceCode: (v.agenceCode || '01001').trim(),
      extype: 'CompteFse',
      serviceType: v.serviceType || null,
      relationId: v.relationId || null,
      mirroirId: null,
    };
    this.saving.set(true);
    this.error.set(null);
    const onOk = () => {
      this.saving.set(false);
      this.dialogVisible = false;
      this.msg.set(this.editId ? 'Compte mis à jour' : 'Compte créé');
      this.reload();
    };
    const onErr = (err: { error?: { message?: string }; message?: string }) => {
      this.saving.set(false);
      this.error.set(err?.error?.message || err?.message || 'Échec enregistrement');
    };
    if (this.editId) {
      this.orch.updateCompteFse(this.editId, body).subscribe({ next: onOk, error: onErr });
    } else {
      this.orch.createCompteFse(body).subscribe({ next: onOk, error: onErr });
    }
  }

  confirmDelete(c: CompteFse): void {
    if (!confirm(`Supprimer le compte ${c.numeroCompte} (${c.typeCompteCode}) ?`)) {
      return;
    }
    this.orch.deleteCompteFse(c.id).subscribe({
      next: () => {
        this.msg.set('Compte supprimé');
        this.reload();
      },
      error: (err) => this.error.set(err?.error?.message || err?.message || 'Échec suppression'),
    });
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.orch.listComptesFse().subscribe({
      next: (list) => {
        this.comptes.set(list ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || err?.message || 'Chargement impossible');
      },
    });
    this.orch.listCorridorsFse().subscribe({
      next: (list) => this.corridors.set(list ?? []),
    });
    this.orch.listTypesCompte().subscribe({
      next: (list) => {
        this.types.set(list ?? []);
        this.typeOptions = (list ?? []).map((t) => ({
          label: `${t.codeParametre} — ${t.libelle}`,
          value: t.codeParametre,
        }));
      },
    });
  }
}
