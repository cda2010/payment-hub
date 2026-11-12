import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import {
  IdentityService,
  Offre,
  Profile,
  ProfileKind,
  UniteProfile
} from '../../core/service/identity.service';

/** Profils SERVICE, TARIFF ou LIMIT affectés à une unité du réseau. */
@Component({
  selector: 'app-unite-profils',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    ButtonModule,
    DialogModule,
    DropdownModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule
  ],
  template: `
    <div class="gw-panel">
      <div class="entete">
        <h3>{{ titre() }} ({{ affectations().length }})</h3>
        @if (peutEcrire) {
          <button pButton type="button" class="p-button-sm" icon="pi pi-plus" label="Affecter" (click)="ouvrir()"></button>
        }
      </div>
      <p class="muted">{{ sousTitre() }}</p>

      @if (loading()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        <p-table [value]="affectations()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Code</th>
              <th>Libellé</th>
              <th>Début</th>
              <th>Fin</th>
              <th>Statut</th>
              @if (peutEcrire) { <th></th> }
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-a>
            <tr [class.inactif]="!a.active">
              <td class="code">{{ a.profileCode || '—' }}</td>
              <td>{{ a.profileLibelle || '—' }}</td>
              <td>{{ a.dateDebut | date: 'dd/MM/yyyy' }}</td>
              <td>{{ a.dateFin ? (a.dateFin | date: 'dd/MM/yyyy') : '—' }}</td>
              <td>
                <p-tag [value]="a.active ? 'Actif' : 'Retiré'" [severity]="a.active ? 'success' : 'secondary'" />
              </td>
              @if (peutEcrire) {
                <td class="actions">
                  @if (a.active) {
                    <button
                      pButton
                      type="button"
                      class="p-button-text p-button-sm p-button-danger"
                      icon="pi pi-ban"
                      title="Retirer"
                      [loading]="enCours() === a.profileId"
                      (click)="retirer(a)"
                    ></button>
                  }
                </td>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td [attr.colspan]="peutEcrire ? 6 : 5">Aucun profil {{ genre() }} affecté.</td></tr>
          </ng-template>
        </p-table>

        @if (montreOffre && offre(); as o) {
          <div class="offre">
            <h4>Offre résolue</h4>
            <p class="muted">Profils actifs à ce jour.</p>
            <p>
              <strong>Services :</strong>
              @for (s of o.services; track s.serviceCode) {
                <code class="chip">{{ s.serviceCode }}</code>
              } @empty { —
              }
            </p>
            <p>
              <strong>Grilles :</strong>
              @for (g of o.grilles; track g.produitCode + (g.canalCode || '')) {
                <code class="chip">{{ g.produitCode }} → {{ g.grilleCode }}</code>
              } @empty { —
              }
            </p>
            <p>
              <strong>Limites :</strong>
              @for (l of o.limites ?? []; track l.produitCode + l.sens) {
                <code class="chip">{{ l.produitCode }} · {{ l.sens }} → {{ l.groupeCode }}</code>
              } @empty { —
              }
            </p>
          </div>
        }
      }
    </div>

    <p-dialog
      [(visible)]="dialogue"
      [header]="'Affecter un profil ' + genre()"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '32rem' }"
    >
      <div class="gw-field">
        <label for="u-profil">Profil *</label>
        <p-dropdown
          inputId="u-profil"
          [options]="catalogue()"
          optionLabel="affichage"
          dataKey="id"
          [(ngModel)]="choix"
          [filter]="true"
          filterBy="affichage"
          placeholder="Choisir un profil"
          styleClass="w-full"
          appendTo="body"
        />
      </div>
      @if (erreurDialogue()) {
        <p class="gw-msg error">{{ erreurDialogue() }}</p>
      }
      <div class="dialog-actions">
        <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogue = false"></button>
        <button pButton type="button" label="Affecter" [loading]="saving()" (click)="affecter()"></button>
      </div>
    </p-dialog>
  `,
  styles: `
    .entete { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .entete h3 { margin: 0; }
    .muted { color: #64748b; font-size: 0.8rem; margin: 0.35rem 0 0.75rem; }
    .code { font-weight: 600; font-family: ui-monospace, monospace; }
    .actions { text-align: right; white-space: nowrap; }
    .inactif { opacity: 0.65; }
    .gw-loading.compact { padding: 0.75rem; }
    .offre { margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid #e2e8f0; }
    .offre h4 { margin: 0 0 0.25rem; font-size: 0.9rem; }
    .chip {
      display: inline-block; margin: 0.15rem 0.25rem 0.15rem 0; padding: 0.1rem 0.4rem;
      background: #f1f5f9; border-radius: 4px; font-size: 0.75rem;
    }
    .gw-field { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.75rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class UniteProfilsComponent implements OnChanges {
  private readonly identity = inject(IdentityService);

  @Input({ required: true }) uniteId!: string;
  @Input() peutEcrire = false;
  @Input() kind: ProfileKind = 'SERVICE';
  @Input() montreOffre = false;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly affectations = signal<UniteProfile[]>([]);
  readonly offre = signal<Offre | null>(null);
  readonly catalogue = signal<(Profile & { affichage: string })[]>([]);
  readonly enCours = signal<string | null>(null);
  readonly saving = signal(false);
  readonly erreurDialogue = signal<string | null>(null);

  dialogue = false;
  choix: (Profile & { affichage: string }) | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['uniteId'] && this.uniteId) {
      this.charger();
    }
  }

  titre(): string {
    if (this.kind === 'TARIFF') {
      return 'Profils tarifaires';
    }
    if (this.kind === 'LIMIT') {
      return 'Profils de limite';
    }
    return 'Profils service';
  }

  sousTitre(): string {
    if (this.kind === 'TARIFF') {
      return 'Grilles de frais de référence pour ce membre du réseau.';
    }
    if (this.kind === 'LIMIT') {
      return 'Groupes de plafonds pour ce membre du réseau. Le profil le plus récent l’emporte pour un même produit et un même sens.';
    }
    return 'Produits / services catalogue autorisés pour ce membre du réseau.';
  }

  genre(): string {
    if (this.kind === 'TARIFF') {
      return 'tarifaire';
    }
    if (this.kind === 'LIMIT') {
      return 'de limite';
    }
    return 'service';
  }

  ouvrir(): void {
    this.choix = null;
    this.erreurDialogue.set(null);
    this.dialogue = true;
    this.identity.listerProfils(null, null, this.kind).subscribe({
      next: (list) =>
        this.catalogue.set(
          list.filter((p) => p.active).map((p) => ({ ...p, affichage: `${p.code} — ${p.libelle}` }))
        ),
      error: (e: { message?: string }) => this.erreurDialogue.set(e?.message ?? 'Catalogue indisponible.')
    });
  }

  affecter(): void {
    if (!this.choix) {
      this.erreurDialogue.set('Choisissez un profil.');
      return;
    }
    this.saving.set(true);
    this.erreurDialogue.set(null);
    this.identity.affecterProfilUnite(this.uniteId, { profileId: this.choix.id }).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogue = false;
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreurDialogue.set(e?.message ?? 'Affectation impossible.');
      }
    });
  }

  retirer(a: UniteProfile): void {
    if (!window.confirm(`Retirer le profil ${a.profileCode} de cette unité ?`)) {
      return;
    }
    this.enCours.set(a.profileId);
    this.identity.retirerProfilUnite(this.uniteId, a.profileId).subscribe({
      next: () => {
        this.enCours.set(null);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.enCours.set(null);
        this.error.set(e?.message ?? 'Retrait impossible.');
      }
    });
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.identity.profilsUnite(this.uniteId).subscribe({
      next: (list) => {
        this.affectations.set(list.filter((a) => (a.profileKind ?? 'SERVICE') === this.kind));
        this.loading.set(false);
        if (this.montreOffre) {
          this.identity.offreUnite(this.uniteId).subscribe({
            next: (o) => this.offre.set(o),
            error: () => this.offre.set(null)
          });
        }
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.affectations.set([]);
        this.offre.set(null);
        this.error.set(
          e?.message ?? 'Profils indisponibles (payment-identity doit tourner sur le port 8094).'
        );
      }
    });
  }
}
