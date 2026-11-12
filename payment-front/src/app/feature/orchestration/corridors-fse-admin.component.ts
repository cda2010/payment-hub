import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
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
  RelationFseCorridor,
  RelationFseCorridorRequest,
  SERVICE_TYPE_CODES,
  ServiceTypeCode,
} from '../../core/model/api.models';

const SERVICE_TYPES = SERVICE_TYPE_CODES;

@Component({
  selector: 'app-corridors-fse-admin',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CheckboxModule,
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
          <h1>Corridors FSE</h1>
          <p class="subtitle">
            Relations inter-FSE (catalogue Omnicanal) — LORO / MIROIR et comptes liés
            @if (!loading()) {
              · <strong>{{ filtered().length }}</strong> / {{ corridors().length }}
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
            label="Nouveau corridor"
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
          placeholder="Rechercher FSE / compte…"
          [ngModel]="query()"
          (ngModelChange)="query.set($event)"
        />
        <p-dropdown
          [options]="fseFilterOptions()"
          [ngModel]="filterSource()"
          (ngModelChange)="filterSource.set($event)"
          placeholder="Source"
          [showClear]="true"
          styleClass="filter-dd"
        />
        <p-dropdown
          [options]="fseFilterOptions()"
          [ngModel]="filterDest()"
          (ngModelChange)="filterDest.set($event)"
          placeholder="Destination"
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
            [rows]="20"
            [rowsPerPageOptions]="[10, 20, 50]"
            [rowHover]="true"
            dataKey="id"
            styleClass="p-datatable-sm"
            [expandedRowKeys]="expandedRows"
            (onRowExpand)="onRowExpand($event)"
            (onRowCollapse)="onRowCollapse($event)"
          >
            <ng-template pTemplate="header">
              <tr>
                <th style="width: 3rem"></th>
                <th>Source</th>
                <th></th>
                <th>Destination</th>
                <th>Service</th>
                <th>LORO</th>
                <th>MIROIR</th>
                <th>Comptes</th>
                <th style="width: 9rem"></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-c let-expanded="expanded">
              <tr>
                <td>
                  <button
                    type="button"
                    pButton
                    [pRowToggler]="c"
                    class="p-button-text p-button-rounded p-button-plain p-button-sm"
                    [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
                  ></button>
                </td>
                <td>
                  <div class="fse-cell">
                    <p-tag [value]="c.fseSource" severity="info" />
                    @if (fseLabel(c.fseSource); as name) {
                      <span class="fse-name">{{ name }}</span>
                    }
                  </div>
                </td>
                <td class="arrow">→</td>
                <td>
                  <div class="fse-cell">
                    <p-tag [value]="c.fseDestination" severity="success" />
                    @if (fseLabel(c.fseDestination); as name) {
                      <span class="fse-name">{{ name }}</span>
                    }
                  </div>
                </td>
                <td><code class="svc">{{ c.serviceType }}</code></td>
                <td>
                  <code>{{ compteNum(c, 'CPTELORO') }}</code>
                  @if (compteNum(c, 'CPTELORO') !== '—') {
                    <span class="hint">{{ '@' + compteFse(c, 'CPTELORO') }}</span>
                  }
                </td>
                <td>
                  <code>{{ compteNum(c, 'CPTEMIROIR') }}</code>
                  @if (compteNum(c, 'CPTEMIROIR') !== '—') {
                    <span class="hint">{{ '@' + compteFse(c, 'CPTEMIROIR') }}</span>
                  }
                </td>
                <td>
                  <span class="count-pill">{{ c.comptes?.length || 0 }}</span>
                </td>
                <td class="actions">
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-text p-button-sm"
                    pTooltip="Modifier LORO / MIROIR"
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
            <ng-template pTemplate="rowexpansion" let-c>
              <tr>
                <td colspan="9">
                  <div class="expand-box">
                    <h4>Comptes liés</h4>
                    <div class="expand-actions">
                      <a
                        class="add-compte"
                        [routerLink]="['/admin/comptes-fse']"
                        [queryParams]="{ relationId: c.id }"
                      >
                        + Ajouter un compte
                      </a>
                    </div>
                    @if (!c.comptes?.length) {
                      <p class="muted">Aucun compte catalogue sur cette relation.</p>
                    } @else {
                      <table class="inner-table">
                        <thead>
                          <tr>
                            <th>Type</th>
                            <th>N°</th>
                            <th>Banque</th>
                            <th>Agence</th>
                            <th>Clé</th>
                            <th>Teneur</th>
                            <th>Titulaire</th>
                            <th>Libellé type</th>
                          </tr>
                        </thead>
                        <tbody>
                          @for (cp of c.comptes; track cp.id) {
                            <tr>
                              <td><p-tag [value]="cp.typeCompteCode" [severity]="typeSeverity(cp.typeCompteCode)" /></td>
                              <td><code>{{ cp.numeroCompte }}</code></td>
                              <td><code>{{ cp.codeBanque || '—' }}</code></td>
                              <td><code>{{ cp.codeAgence || cp.agenceCode || '—' }}</code></td>
                              <td>{{ cp.cle || '—' }}</td>
                              <td>
                                <span class="fse-inline">{{ cp.fseTeneur }}</span>
                                @if (fseLabel(cp.fseTeneur); as name) {
                                  <span class="fse-name">{{ name }}</span>
                                }
                              </td>
                              <td><code>{{ cp.fseTitulaire || '—' }}</code></td>
                              <td>{{ cp.typeCompteLibelle || '—' }}</td>
                            </tr>
                          }
                        </tbody>
                      </table>
                    }
                  </div>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="9">
                  Aucun corridor
                  @if (corridors().length) {
                    pour ce filtre
                  } @else {
                    — importez depuis Omnicanal ou créez un corridor.
                  }
                </td>
              </tr>
            </ng-template>
          </p-table>
        </div>

        <div class="gw-panel help">
          <h3>Rappel schéma DIFF_FSE</h3>
          <ul>
            <li>
              <strong>Jambe source</strong> : débit client, crédit <code>CPTELORO</code> (chez FSE source)
            </li>
            <li>
              <strong>Jambe dest</strong> : débit <code>CPTEMIROIR</code> (chez FSE dest), crédit client
            </li>
            <li>Les relations affichées viennent du catalogue (import Omnicanal + saisie admin).</li>
          </ul>
        </div>
      }
    </div>

    <p-dialog
      [(visible)]="dialogVisible"
      [header]="editId ? 'Modifier le corridor' : 'Nouveau corridor'"
      [modal]="true"
      [style]="{ width: '36rem' }"
      [draggable]="false"
    >
      <form [formGroup]="form" class="dialog-form" (ngSubmit)="save()">
        <label>
          FSE source
          <p-dropdown
            formControlName="fseSource"
            [options]="fseOptions"
            [editable]="true"
            placeholder="ex. 0004"
            styleClass="w-full"
          />
        </label>
        <label>
          FSE destination
          <p-dropdown
            formControlName="fseDestination"
            [options]="fseOptions"
            [editable]="true"
            placeholder="ex. 0011"
            styleClass="w-full"
          />
        </label>
        <label>
          Type de service
          <p-dropdown
            formControlName="serviceType"
            [options]="serviceTypeOptions"
            optionLabel="label"
            optionValue="value"
            styleClass="w-full"
          />
        </label>
        <label>
          N° compte LORO (chez FSE source)
          <input pInputText formControlName="numeroCompteLoroSource" class="w-full" />
        </label>
        <label>
          N° compte MIROIR (chez FSE dest)
          <input pInputText formControlName="numeroCompteMiroirDest" class="w-full" />
        </label>
        @if (!editId) {
          <div class="check-row">
            <p-checkbox formControlName="alsoCreateReverse" [binary]="true" inputId="rev" />
            <label for="rev">Créer aussi le sens inverse (recommandé)</label>
          </div>
        }
        <div class="dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogVisible = false"></button>
          <button
            pButton
            type="submit"
            [label]="editId ? 'Enregistrer' : 'Créer'"
            [disabled]="form.invalid || saving()"
          ></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .filters {
      display: flex;
      flex-wrap: wrap;
      gap: 0.65rem;
      align-items: center;
      margin-bottom: 0.75rem;
    }
    .filter-q { flex: 1 1 12rem; min-width: 10rem; }
    :host ::ng-deep .filter-dd { min-width: 9rem; }
    .arrow {
      text-align: center;
      color: #64748b;
      font-weight: 600;
    }
    .fse-cell {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }
    .fse-name {
      font-size: 0.75rem;
      color: #64748b;
      max-width: 11rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .fse-inline { font-family: ui-monospace, monospace; font-size: 0.85rem; margin-right: 0.35rem; }
    .svc { font-size: 0.8rem; }
    code { font-size: 0.85rem; }
    .hint {
      margin-left: 0.35rem;
      color: #64748b;
      font-size: 0.75rem;
    }
    .count-pill {
      display: inline-block;
      min-width: 1.5rem;
      text-align: center;
      padding: 0.1rem 0.45rem;
      border-radius: 999px;
      background: #f1f5f9;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .actions { white-space: nowrap; }
    .expand-box {
      padding: 0.75rem 1rem 1rem;
      background: #f8fafc;
      border-radius: 8px;
      margin: 0.25rem 0 0.5rem;
    }
    .expand-box h4 { margin: 0 0 0.5rem; font-size: 0.9rem; }
    .expand-actions { margin-bottom: 0.65rem; }
    .add-compte {
      font-size: 0.85rem;
      color: var(--primary-color, #2563eb);
      text-decoration: none;
      font-weight: 500;
    }
    .add-compte:hover { text-decoration: underline; }
    .muted { color: #94a3b8; margin: 0; font-size: 0.85rem; }
    .inner-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
    }
    .inner-table th {
      text-align: left;
      color: #64748b;
      font-weight: 600;
      padding: 0.35rem 0.5rem;
      border-bottom: 1px solid #e2e8f0;
    }
    .inner-table td {
      padding: 0.4rem 0.5rem;
      border-bottom: 1px solid #f1f5f9;
      vertical-align: middle;
    }
    .help ul {
      margin: 0.5rem 0 0;
      padding-left: 1.2rem;
      line-height: 1.55;
    }
    .dialog-form {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
    }
    .dialog-form label {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      font-size: 0.85rem;
      font-weight: 500;
    }
    .w-full { width: 100%; }
    .check-row {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9rem;
    }
    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
      margin-top: 0.5rem;
    }
  `,
})
export class CorridorsFseAdminComponent implements OnInit {
  private readonly orch = inject(OrchestrationService);
  private readonly providers = inject(ProviderService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly corridors = signal<RelationFseCorridor[]>([]);
  readonly query = signal('');
  readonly filterSource = signal<string | null>(null);
  readonly filterDest = signal<string | null>(null);
  readonly filterService = signal<ServiceTypeCode | null>(null);

  /** codeFse → libellé provider */
  private readonly fseNames = signal<Record<string, string>>({});

  dialogVisible = false;
  editId: string | null = null;
  fseOptions: string[] = [];
  expandedRows: Record<string, boolean> = {};
  serviceTypeOptions = SERVICE_TYPES.map((v) => ({ label: v, value: v }));

  readonly fseFilterOptions = computed(() => {
    const set = new Set<string>();
    for (const c of this.corridors()) {
      set.add(c.fseSource);
      set.add(c.fseDestination);
    }
    return [...set].sort();
  });

  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const src = this.filterSource();
    const dest = this.filterDest();
    const svc = this.filterService();
    return this.corridors().filter((c) => {
      if (src && c.fseSource !== src) {
        return false;
      }
      if (dest && c.fseDestination !== dest) {
        return false;
      }
      if (svc && c.serviceType !== svc) {
        return false;
      }
      if (!q) {
        return true;
      }
      const labelSrc = (this.fseNames()[c.fseSource] || '').toLowerCase();
      const labelDest = (this.fseNames()[c.fseDestination] || '').toLowerCase();
      const nums = (c.comptes || []).map((x) => x.numeroCompte).join(' ').toLowerCase();
      const types = (c.comptes || []).map((x) => x.typeCompteCode).join(' ').toLowerCase();
      return (
        c.fseSource.toLowerCase().includes(q) ||
        c.fseDestination.toLowerCase().includes(q) ||
        c.serviceType.toLowerCase().includes(q) ||
        labelSrc.includes(q) ||
        labelDest.includes(q) ||
        nums.includes(q) ||
        types.includes(q)
      );
    });
  });

  readonly form = this.fb.nonNullable.group({
    fseSource: ['', Validators.required],
    fseDestination: ['', Validators.required],
    serviceType: this.fb.nonNullable.control<ServiceTypeCode>('TRANSFERT', Validators.required),
    numeroCompteLoroSource: ['', Validators.required],
    numeroCompteMiroirDest: ['', Validators.required],
    alsoCreateReverse: true,
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

  compteNum(c: RelationFseCorridor, type: string): string {
    return c.comptes.find((x) => x.typeCompteCode === type)?.numeroCompte ?? '—';
  }

  compteFse(c: RelationFseCorridor, type: string): string {
    return c.comptes.find((x) => x.typeCompteCode === type)?.fseTeneur ?? '—';
  }

  openCreate(): void {
    this.editId = null;
    this.form.reset({
      fseSource: '',
      fseDestination: '',
      serviceType: 'TRANSFERT',
      numeroCompteLoroSource: '',
      numeroCompteMiroirDest: '',
      alsoCreateReverse: true,
    });
    this.dialogVisible = true;
  }

  openEdit(c: RelationFseCorridor): void {
    this.editId = c.id;
    this.form.reset({
      fseSource: c.fseSource,
      fseDestination: c.fseDestination,
      serviceType: c.serviceType,
      numeroCompteLoroSource: this.compteNum(c, 'CPTELORO') === '—' ? '' : this.compteNum(c, 'CPTELORO'),
      numeroCompteMiroirDest:
        this.compteNum(c, 'CPTEMIROIR') === '—' ? '' : this.compteNum(c, 'CPTEMIROIR'),
      alsoCreateReverse: false,
    });
    this.dialogVisible = true;
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const body: RelationFseCorridorRequest = {
      fseSource: v.fseSource.trim(),
      fseDestination: v.fseDestination.trim(),
      serviceType: v.serviceType,
      numeroCompteLoroSource: v.numeroCompteLoroSource.trim(),
      numeroCompteMiroirDest: v.numeroCompteMiroirDest.trim(),
      alsoCreateReverse: !this.editId && !!v.alsoCreateReverse,
    };
    this.saving.set(true);
    this.error.set(null);
    const onOk = (res: RelationFseCorridor | RelationFseCorridor[]) => {
      this.saving.set(false);
      this.dialogVisible = false;
      const n = Array.isArray(res) ? res.length : 1;
      this.msg.set(this.editId ? 'Corridor mis à jour' : `${n} corridor(s) créé(s)`);
      this.reload();
    };
    const onErr = (err: { error?: { message?: string }; message?: string }) => {
      this.saving.set(false);
      this.error.set(err?.error?.message || err?.message || 'Échec enregistrement');
    };
    if (this.editId) {
      this.orch.updateCorridorFse(this.editId, body).subscribe({ next: onOk, error: onErr });
    } else {
      this.orch.createCorridorFse(body).subscribe({ next: onOk, error: onErr });
    }
  }

  confirmDelete(c: RelationFseCorridor): void {
    if (!confirm(`Supprimer le corridor ${c.fseSource} → ${c.fseDestination} (${c.serviceType}) ?`)) {
      return;
    }
    this.orch.deleteCorridorFse(c.id).subscribe({
      next: () => {
        this.msg.set('Corridor supprimé');
        this.reload();
      },
      error: (err) => this.error.set(err?.error?.message || err?.message || 'Échec suppression'),
    });
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.orch.listCorridorsFse().subscribe({
      next: (list) => {
        this.corridors.set(list ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || err?.message || 'Chargement impossible');
      },
    });
  }

  onRowExpand(ev: { data?: RelationFseCorridor }): void {
    if (ev?.data?.id) {
      this.expandedRows = { ...this.expandedRows, [ev.data.id]: true };
    }
  }

  onRowCollapse(ev: { data?: RelationFseCorridor }): void {
    if (ev?.data?.id) {
      const next = { ...this.expandedRows };
      delete next[ev.data.id];
      this.expandedRows = next;
    }
  }
}
