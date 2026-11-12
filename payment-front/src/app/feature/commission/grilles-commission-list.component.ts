import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { CommissionService, GrilleCommission } from '../../core/service/commission.service';
import { GrilleCommissionFormDialogComponent } from './grille-commission-form-dialog.component';

@Component({
  selector: 'app-grilles-commission-list',
  standalone: true,
  imports: [
    RouterLink, ButtonModule, ProgressSpinnerModule, TableModule, TagModule,
    GrilleCommissionFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Grilles de commissionnement</h1>
          <p class="subtitle">
            Répartition des droits entre acteurs (distinct des grilles tarifaires)
            @if (!loading()) {
              · <strong>{{ grilles().length }}</strong>
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-play" label="Simulateur" routerLink="/admin/commission/simulateur"></a>
          <button pButton icon="pi pi-plus" label="Nouvelle grille" (click)="ouvrirCreate()"></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <p-table [value]="grilles()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Opération</th>
                <th>Canal</th>
                <th>Devise</th>
                <th>Version</th>
                <th>Statut</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-g>
              <tr>
                <td class="code">
                  <a class="lien" [routerLink]="['/admin/commission/grilles', g.id]">{{ g.code }}</a>
                </td>
                <td>{{ g.libelle }}</td>
                <td>{{ g.operationType }}</td>
                <td>{{ g.channelCode || '—' }}</td>
                <td>{{ g.currency }}</td>
                <td>v{{ g.version }}</td>
                <td>
                  <p-tag [value]="g.status" [severity]="g.status === 'PUBLIEE' ? 'success' : 'secondary'" />
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="7">Aucune grille. Créez-en une ou démarrez payment-commission (:8098).</td></tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <app-grille-commission-form-dialog
      [(visible)]="dialogVisible"
      [grille]="null"
      (saved)="onCreated($event)"
    />
  `,
  styles: `
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    .lien { color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover { text-decoration: underline; }
    .header-actions { display: flex; gap: 0.5rem; align-items: center; }
  `
})
export class GrillesCommissionListComponent implements OnInit {
  private readonly api = inject(CommissionService);
  private readonly router = inject(Router);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly grilles = signal<GrilleCommission[]>([]);
  dialogVisible = false;

  ngOnInit(): void {
    this.reload();
  }

  ouvrirCreate(): void {
    this.dialogVisible = true;
  }

  onCreated(g: GrilleCommission): void {
    void this.router.navigate(['/admin/commission/grilles', g.id]);
  }

  private reload(): void {
    this.loading.set(true);
    this.api.listerGrilles().subscribe({
      next: (list) => {
        this.grilles.set(list);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible (payment-commission :8098).');
      }
    });
  }
}
