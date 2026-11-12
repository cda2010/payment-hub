import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { CatalogParticipant, OpsService } from '../../core/service/ops.service';

@Component({
  selector: 'app-catalog-admin',
  standalone: true,
  imports: [
    FormsModule,
    ProgressSpinnerModule,
    ButtonModule,
    InputTextModule,
    TableModule,
    TagModule,
    TooltipModule,
    RouterLink
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Catalogue FSE</h1>
          <p class="subtitle">
            Participants et capacités (équivalent Omnicanal /api/v1/catalog)
            @if (!loading() && !error()) {
              · <strong>{{ filtered().length }}</strong> / {{ participants().length }}
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-text p-button-sm" routerLink="/admin/providers" label="Fournisseurs"></a>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
      } @else if (!error()) {
        <div class="gw-panel filters">
          <input
            pInputText
            class="filter-q"
            placeholder="Nom, code FSE, provider…"
            [ngModel]="query()"
            (ngModelChange)="query.set($event)"
          />
        </div>

        <div class="gw-panel">
          <p-table
            [value]="filtered()"
            [paginator]="true"
            [rows]="10"
            [rowsPerPageOptions]="[10, 25, 50]"
            [rowHover]="true"
            dataKey="participantCode"
            styleClass="p-datatable-sm p-datatable-striped"
            [expandedRowKeys]="expandedRows"
            (onRowExpand)="onRowExpand($event)"
            (onRowCollapse)="onRowCollapse($event)"
          >
            <ng-template pTemplate="header">
              <tr>
                <th style="width: 3rem"></th>
                <th>FSE</th>
                <th>Nom</th>
                <th>Provider</th>
                <th>Type</th>
                <th>Statut</th>
                <th>Capacités</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-p let-expanded="expanded">
              <tr>
                <td>
                  <button
                    type="button"
                    pButton
                    [pRowToggler]="p"
                    class="p-button-text p-button-rounded p-button-plain p-button-sm"
                    [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
                    pTooltip="Capacités"
                  ></button>
                </td>
                <td><code>{{ p.fseCode }}</code></td>
                <td>{{ p.name }}</td>
                <td><code>{{ p.providerCode }}</code></td>
                <td>{{ p.type || '—' }}</td>
                <td>
                  <p-tag [value]="p.enabled ? 'Actif' : 'Inactif'" [severity]="p.enabled ? 'success' : 'danger'" />
                </td>
                <td>{{ p.capabilities?.length || 0 }}</td>
              </tr>
            </ng-template>
            <ng-template pTemplate="rowexpansion" let-p>
              <tr>
                <td colspan="7">
                  <div class="expand-box">
                    @if (!p.capabilities?.length) {
                      <p class="muted">Aucune capacité.</p>
                    } @else {
                      <table class="inner-table">
                        <thead>
                          <tr>
                            <th>Capacité</th>
                            <th>Code</th>
                            <th>Catégorie</th>
                            <th>Statut</th>
                            <th>Opérations</th>
                          </tr>
                        </thead>
                        <tbody>
                          @for (c of p.capabilities; track c.capabilityCode) {
                            <tr>
                              <td>{{ c.label }}</td>
                              <td><code>{{ c.capabilityCode }}</code></td>
                              <td>{{ c.category || '—' }}</td>
                              <td>{{ c.enabled ? 'Actif' : 'Inactif' }}</td>
                              <td>
                                @for (op of c.operations; track op.operationCode) {
                                  <span class="op-tag" [pTooltip]="op.nom || ''">{{ op.operationCode }}</span>
                                }
                                @if (!c.operations?.length) {
                                  <span class="muted">—</span>
                                }
                              </td>
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
                <td colspan="7">
                  @if (participants().length) {
                    Aucun FSE pour ce filtre.
                  } @else {
                    Aucun participant dans le catalogue.
                  }
                </td>
              </tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>
  `,
  styles: `
    .header-actions { display: flex; align-items: center; }
    .filters {
      display: flex;
      margin-bottom: 1rem;
    }
    .filter-q { min-width: 16rem; flex: 1; }
    .expand-box { padding: 0.5rem 0.25rem 0.75rem 2.5rem; }
    .inner-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .inner-table th, .inner-table td {
      text-align: left;
      padding: 0.4rem 0.6rem;
      border-bottom: 1px solid #e2e8f0;
      vertical-align: top;
    }
    .inner-table th { color: #64748b; font-weight: 600; }
    .op-tag {
      display: inline-block;
      margin: 0.1rem;
      padding: 0.1rem 0.4rem;
      background: #f1f5f9;
      border-radius: 4px;
      font-size: 0.8rem;
    }
    .muted { color: #94a3b8; margin: 0; }
    code { font-size: 0.85em; }
  `
})
export class CatalogAdminComponent implements OnInit {
  private readonly opsApi = inject(OpsService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly participants = signal<CatalogParticipant[]>([]);
  readonly query = signal('');
  expandedRows: Record<string, boolean> = {};

  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const list = this.participants();
    if (!q) {
      return list;
    }
    return list.filter(p =>
      [p.name, p.fseCode, p.providerCode, p.participantCode, p.type]
        .some(v => (v || '').toLowerCase().includes(q))
    );
  });

  onRowExpand(ev: { data?: CatalogParticipant }): void {
    const key = ev?.data?.participantCode;
    if (key) {
      this.expandedRows = { ...this.expandedRows, [key]: true };
    }
  }

  onRowCollapse(ev: { data?: CatalogParticipant }): void {
    const key = ev?.data?.participantCode;
    if (!key) {
      return;
    }
    const next = { ...this.expandedRows };
    delete next[key];
    this.expandedRows = next;
  }

  ngOnInit(): void {
    this.opsApi.listParticipants().subscribe({
      next: (list) => {
        this.participants.set(list);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Catalogue indisponible');
        this.loading.set(false);
      }
    });
  }
}
