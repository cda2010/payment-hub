import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import {
  CommissionService,
  FormuleCommission,
  GrilleCommission,
  RegleCommission
} from '../../core/service/commission.service';
import { GrilleCommissionFormDialogComponent } from './grille-commission-form-dialog.component';
import { RegleCommissionFormDialogComponent } from './regle-commission-form-dialog.component';

@Component({
  selector: 'app-grille-commission-detail',
  standalone: true,
  imports: [
    RouterLink, ButtonModule, ProgressSpinnerModule, TableModule, TagModule,
    GrilleCommissionFormDialogComponent, RegleCommissionFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (!grille()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a routerLink="/admin/commission/grilles">Retour</a>
      } @else {
        @if (grille(); as g) {
          <div class="gw-page-header">
            <div>
              <nav class="fil">
                <a routerLink="/admin/commission/grilles">Grilles</a>
                <i class="pi pi-angle-right"></i>
                <span>{{ g.code }}</span>
              </nav>
              <h1>
                {{ g.code }}
                <p-tag [value]="g.status" [severity]="g.status === 'PUBLIEE' ? 'success' : 'secondary'" />
              </h1>
              <p class="subtitle">{{ g.libelle }} · v{{ g.version }}</p>
            </div>
            <div class="header-actions">
              @if (editable()) {
                <button pButton class="p-button-outlined" icon="pi pi-pencil" label="Modifier" (click)="editGrille = true"></button>
                <button pButton class="p-button-outlined p-button-danger" icon="pi pi-trash" label="Supprimer" (click)="supprimerGrille()"></button>
                <button pButton icon="pi pi-check" label="Publier" (click)="publier()" [disabled]="!g.regles.length"></button>
              } @else {
                <button pButton class="p-button-outlined" icon="pi pi-copy" label="Nouvelle version" (click)="nouvelleVersion()"></button>
              }
            </div>
          </div>

          @if (actionError()) {
            <p class="gw-msg error">{{ actionError() }}</p>
          }

          <div class="gw-panel">
            <h3>Contexte</h3>
            <dl class="fiche">
              <dt>Opération</dt><dd>{{ g.operationType }}</dd>
              <dt>Produit</dt><dd>{{ g.productCode || '—' }}</dd>
              <dt>Canal</dt><dd>{{ g.channelCode || '—' }}</dd>
              <dt>Devise</dt><dd>{{ g.currency }}</dd>
              <dt>Enveloppe</dt><dd>{{ g.envelopeMode }}</dd>
              <dt>Validité</dt><dd>{{ g.validFrom }} → {{ g.validTo || '∞' }}</dd>
            </dl>
          </div>

          <div class="gw-panel">
            <div class="panel-head">
              <h3>Règles ({{ g.regles.length }})</h3>
              @if (editable()) {
                <button pButton icon="pi pi-plus" label="Ajouter une règle" class="p-button-sm" (click)="ouvrirRegle()"></button>
              }
            </div>
            @if (!editable()) {
              <p class="hint">Grille publiée — créez une nouvelle version pour modifier les règles.</p>
            }
            <p-table [value]="g.regles" styleClass="p-datatable-sm" dataKey="id">
              <ng-template pTemplate="header">
                <tr>
                  <th>Priorité</th>
                  <th>Code</th>
                  <th>Bénéficiaire</th>
                  <th>Formule</th>
                  <th>Base</th>
                  <th>Params</th>
                  <th>Financement</th>
                  @if (editable()) { <th></th> }
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-r>
                <tr>
                  <td>{{ r.priority }}</td>
                  <td class="code">{{ r.code }}</td>
                  <td>{{ r.beneficiaryRole }}</td>
                  <td>{{ r.formuleCode }} v{{ r.formuleVersion }}</td>
                  <td>{{ r.commissionBase }}</td>
                  <td><code>{{ r.paramsJson }}</code></td>
                  <td>{{ r.fundingType }}</td>
                  @if (editable()) {
                    <td class="actions">
                      <button pButton class="p-button-text p-button-sm" icon="pi pi-pencil" (click)="ouvrirRegle(r)"></button>
                      <button pButton class="p-button-text p-button-danger p-button-sm" icon="pi pi-trash" (click)="supprimerRegle(r)"></button>
                    </td>
                  }
                </tr>
              </ng-template>
              <ng-template pTemplate="emptymessage">
                <tr><td [attr.colspan]="editable() ? 8 : 7">Aucune règle. Ajoutez-en pour pouvoir publier.</td></tr>
              </ng-template>
            </p-table>
          </div>
        }
      }
    </div>

    <app-grille-commission-form-dialog
      [(visible)]="editGrille"
      [grille]="grille()"
      (saved)="onGrilleSaved($event)"
    />
    <app-regle-commission-form-dialog
      [(visible)]="editRegle"
      [grilleId]="grille()?.id ?? ''"
      [regle]="regleCourante"
      [formules]="formules()"
      (saved)="reload()"
    />
  `,
  styles: `
    .fil { display: flex; gap: 0.35rem; align-items: center; font-size: 0.85rem; color: #64748b; margin-bottom: 0.35rem; }
    .fil a { color: var(--primary-color, #2563eb); text-decoration: none; }
    .fiche { display: grid; grid-template-columns: 9rem 1fr 9rem 1fr; gap: 0.35rem 0.75rem; margin: 0; font-size: 0.88rem; }
    .fiche dt { color: #64748b; }
    .fiche dd { margin: 0; }
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    h3 { margin: 0; font-size: 0.95rem; }
    .panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .header-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .actions { white-space: nowrap; }
    .hint { font-size: 0.85rem; color: #64748b; margin: 0 0 0.75rem; }
  `
})
export class GrilleCommissionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(CommissionService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly grille = signal<GrilleCommission | null>(null);
  readonly formules = signal<FormuleCommission[]>([]);

  editGrille = false;
  editRegle = false;
  regleCourante: RegleCommission | null = null;

  editable(): boolean {
    return this.grille()?.status === 'BROUILLON';
  }

  ngOnInit(): void {
    this.api.listerFormules().subscribe({ next: (f) => this.formules.set(f) });
    this.reload();
  }

  reload(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading.set(false);
      this.error.set('Grille introuvable.');
      return;
    }
    this.loading.set(true);
    this.api.grille(id).subscribe({
      next: (g) => {
        this.grille.set(g);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  onGrilleSaved(g: GrilleCommission): void {
    this.grille.set(g);
  }

  ouvrirRegle(r?: RegleCommission): void {
    this.regleCourante = r ?? null;
    this.editRegle = true;
  }

  publier(): void {
    const g = this.grille();
    if (!g) return;
    this.actionError.set(null);
    this.api.publier(g.id).subscribe({
      next: (updated) => this.grille.set(updated),
      error: (e: { message?: string }) => this.actionError.set(e?.message ?? 'Publication impossible.')
    });
  }

  nouvelleVersion(): void {
    const g = this.grille();
    if (!g) return;
    this.actionError.set(null);
    this.api.nouvelleVersion(g.id).subscribe({
      next: (v) => void this.router.navigate(['/admin/commission/grilles', v.id]),
      error: (e: { message?: string }) => this.actionError.set(e?.message ?? 'Copie impossible.')
    });
  }

  supprimerGrille(): void {
    const g = this.grille();
    if (!g || !confirm(`Supprimer le brouillon ${g.code} v${g.version} ?`)) return;
    this.api.supprimerGrille(g.id).subscribe({
      next: () => void this.router.navigate(['/admin/commission/grilles']),
      error: (e: { message?: string }) => this.actionError.set(e?.message ?? 'Suppression impossible.')
    });
  }

  supprimerRegle(r: RegleCommission): void {
    const g = this.grille();
    if (!g || !confirm(`Supprimer la règle ${r.code} ?`)) return;
    this.api.supprimerRegle(g.id, r.id).subscribe({
      next: () => this.reload(),
      error: (e: { message?: string }) => this.actionError.set(e?.message ?? 'Suppression impossible.')
    });
  }
}
