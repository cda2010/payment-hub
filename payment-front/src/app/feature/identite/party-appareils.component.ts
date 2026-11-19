import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Appareil, IdentityService } from '../../core/service/identity.service';

/** Appareils de confiance enregistrés pour une personne. */
@Component({
  selector: 'app-party-appareils',
  standalone: true,
  imports: [ButtonModule, ProgressSpinnerModule, TableModule, TagModule],
  template: `
    <div class="gw-panel">
      <h3>Appareils ({{ appareils().length }})</h3>
      <p class="muted">Téléphones et navigateurs autorisés à garder une session. Le code secret n’est pas conservé.</p>
      @if (loading()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        <p-table [value]="appareils()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Nom</th>
              <th>Plateforme</th>
              <th>Statut</th>
              <th>Dernière activité</th>
              @if (peutEcrire) {
                <th></th>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-a>
            <tr>
              <td>{{ a.nom }}</td>
              <td>{{ a.plateforme }}</td>
              <td>
                <p-tag [value]="a.statut === 'ACTIF' ? 'Actif' : 'Révoqué'" [severity]="a.statut === 'ACTIF' ? 'success' : 'secondary'" />
              </td>
              <td>{{ format(a.vuLe) }}</td>
              @if (peutEcrire) {
                <td class="actions">
                  @if (a.statut === 'ACTIF') {
                    <button
                      pButton
                      type="button"
                      class="p-button-text p-button-sm p-button-danger"
                      icon="pi pi-ban"
                      label="Révoquer"
                      [disabled]="enCours()"
                      (click)="revoquer(a)"
                    ></button>
                  }
                </td>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td [attr.colspan]="peutEcrire ? 5 : 4">Aucun appareil enregistré.</td>
            </tr>
          </ng-template>
        </p-table>
      }
    </div>
  `,
  styles: `
    .muted { margin: 0 0 0.75rem; color: #64748b; font-size: 0.85rem; }
    h3 { font-size: 0.95rem; margin: 0 0 0.35rem; }
    .actions { text-align: right; }
  `
})
export class PartyAppareilsComponent implements OnChanges {
  private readonly identity = inject(IdentityService);

  @Input({ required: true }) partyId!: string;
  @Input() peutEcrire = false;

  readonly appareils = signal<Appareil[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly enCours = signal(false);

  ngOnChanges(): void {
    if (this.partyId) this.charger();
  }

  format(iso: string): string {
    if (!iso) return '—';
    const date = new Date(iso);
    return Number.isNaN(date.getTime()) ? iso : date.toLocaleString('fr-FR');
  }

  revoquer(appareil: Appareil): void {
    this.enCours.set(true);
    this.error.set(null);
    this.identity.revoquerAppareil(this.partyId, appareil.id).subscribe({
      next: () => {
        this.enCours.set(false);
        this.charger();
      },
      error: (e: { error?: { message?: string }; message?: string }) => {
        this.enCours.set(false);
        this.error.set(e?.error?.message || e?.message || 'La révocation a échoué.');
      }
    });
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.identity.appareilsParty(this.partyId).subscribe({
      next: (liste) => {
        this.appareils.set(liste);
        this.loading.set(false);
      },
      error: (e: { error?: { message?: string }; message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.error?.message || e?.message || 'Chargement impossible.');
      }
    });
  }
}
