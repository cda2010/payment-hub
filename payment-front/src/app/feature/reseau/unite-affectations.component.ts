import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AffectationCaissier, NetworkService, PointVente, UtilisateurUnite } from '../../core/service/network.service';

/** Affectation d'un caissier à un point de vente, avec une période et un motif de sortie. */
@Component({
  selector: 'app-unite-affectations',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, DropdownModule, InputTextModule, ProgressSpinnerModule, TableModule, TagModule],
  template: `
    <div class="gw-panel">
      <div class="entete">
        <h3>Affectations ({{ actives() }})</h3>
        @if (peutEcrire) {
          <button pButton type="button" class="p-button-sm" icon="pi pi-user-plus" label="Affecter un caissier" (click)="ouvrir()"></button>
        }
      </div>
      <p class="muted">Un caissier tient un point de vente à partir d'une date. La date de fin le retire le lendemain. Une sortie immédiate exige un motif.</p>

      @if (loading()) {
        <div class="gw-loading compact"><p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" /></div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else if (!points().length) {
        <p class="muted">Cette unité n'a pas encore de point de vente.</p>
      } @else {
        <p-table [value]="lignes()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Point de vente</th>
              <th>Caissier</th>
              <th>Début</th>
              <th>Fin</th>
              <th>Statut</th>
              @if (peutEcrire) { <th></th> }
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-a>
            <tr>
              <td class="code">{{ a.pointCode }}</td>
              <td>{{ a.nom }} <span class="code">{{ a.login }}</span></td>
              <td>{{ a.debut }}</td>
              <td>{{ a.fin || '—' }}</td>
              <td>
                <p-tag [value]="libelle(a.statut)" [severity]="a.effective ? 'success' : 'secondary'" />
                @if (a.motif) { <span class="motif">{{ a.motif }}</span> }
              </td>
              @if (peutEcrire) {
                <td class="actions">
                  @if (a.statut === 'ACTIF' || a.statut === 'A_VENIR') {
                    <button pButton type="button" class="p-button-text p-button-sm p-button-danger" icon="pi pi-user-minus" label="Désaffecter" (click)="ouvrirSortie(a)"></button>
                  }
                </td>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td [attr.colspan]="peutEcrire ? 6 : 5">Aucun caissier affecté.</td></tr>
          </ng-template>
        </p-table>
      }
    </div>

    <p-dialog [(visible)]="dialogue" header="Affecter un caissier" [modal]="true" [draggable]="false" [style]="{ width: '32rem' }">
      <form class="gw-form-grid" (ngSubmit)="affecter()">
        <div class="gw-field full">
          <label for="af-point">Point de vente *</label>
          <p-dropdown inputId="af-point" name="point" [options]="points()" optionLabel="libelle" optionValue="id" [(ngModel)]="s.pointId" styleClass="w-full" appendTo="body" />
        </div>
        <div class="gw-field full">
          <label for="af-caisse">Caissier *</label>
          <p-dropdown inputId="af-caisse" name="caissier" [options]="caissiers()" optionLabel="nom" optionValue="id" [(ngModel)]="s.utilisateurId" styleClass="w-full" appendTo="body" />
        </div>
        <div class="gw-field">
          <label for="af-debut">Début *</label>
          <input pInputText id="af-debut" type="date" name="debut" [(ngModel)]="s.debut" />
        </div>
        <div class="gw-field">
          <label for="af-fin">Fin</label>
          <input pInputText id="af-fin" type="date" name="fin" [(ngModel)]="s.fin" />
        </div>
        @if (erreur()) { <p class="gw-msg error full">{{ erreur() }}</p> }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogue = false"></button>
          <button pButton type="submit" label="Affecter" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>

    <p-dialog [(visible)]="dialogueSortie" header="Désaffecter le caissier" [modal]="true" [draggable]="false" [style]="{ width: '28rem' }">
      <form class="gw-form-grid" (ngSubmit)="desaffecter()">
        <p class="muted full">{{ cible?.nom }} quitte {{ cible?.pointCode }}. Le motif reste dans l'historique.</p>
        <div class="gw-field full">
          <label for="af-motif">Motif *</label>
          <input pInputText id="af-motif" name="motif" [(ngModel)]="motif" maxlength="200" />
        </div>
        @if (erreurSortie()) { <p class="gw-msg error full">{{ erreurSortie() }}</p> }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogueSortie = false"></button>
          <button pButton type="submit" label="Désaffecter" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .entete { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .entete h3 { margin: 0; }
    .muted { color: #64748b; font-size: 0.8rem; margin: 0.35rem 0 0.75rem; }
    .code { font-weight: 600; font-family: ui-monospace, monospace; }
    .motif { display: block; color: #64748b; font-size: 0.75rem; margin-top: 0.15rem; }
    .actions { text-align: right; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class UniteAffectationsComponent implements OnChanges {
  private readonly network = inject(NetworkService);

  @Input({ required: true }) uniteId!: string;
  @Input() peutEcrire = false;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly points = signal<PointVente[]>([]);
  readonly lignes = signal<AffectationCaissier[]>([]);
  readonly caissiers = signal<UtilisateurUnite[]>([]);
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly erreurSortie = signal<string | null>(null);

  dialogue = false;
  dialogueSortie = false;
  s = this.vide();
  cible: AffectationCaissier | null = null;
  motif = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['uniteId'] && this.uniteId) {
      this.charger();
    }
  }

  actives(): number {
    return this.lignes().filter((l) => l.effective).length;
  }

  ouvrir(): void {
    this.s = this.vide();
    this.erreur.set(null);
    this.dialogue = true;
  }

  ouvrirSortie(a: AffectationCaissier): void {
    this.cible = a;
    this.motif = '';
    this.erreurSortie.set(null);
    this.dialogueSortie = true;
  }

  affecter(): void {
    if (!this.s.pointId || !this.s.utilisateurId || !this.s.debut) {
      this.erreur.set('Le point de vente, le caissier et la date de début sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.erreur.set(null);
    this.network.affecterCaissier(this.s.pointId, {
      utilisateurId: this.s.utilisateurId,
      debut: this.s.debut,
      fin: this.s.fin || undefined
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogue = false;
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Affectation impossible.');
      }
    });
  }

  desaffecter(): void {
    if (!this.cible || this.motif.trim().length < 3) {
      this.erreurSortie.set('Le motif de désaffectation est obligatoire.');
      return;
    }
    this.saving.set(true);
    this.erreurSortie.set(null);
    this.network.desaffecterCaissier(this.cible.id, this.motif.trim()).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogueSortie = false;
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreurSortie.set(e?.message ?? 'Désaffectation impossible.');
      }
    });
  }

  libelle(statut: AffectationCaissier['statut']): string {
    if (statut === 'ACTIF') return 'En poste';
    if (statut === 'A_VENIR') return 'À venir';
    if (statut === 'ECHUE') return 'Échue';
    return 'Close';
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.network.pointsDeVente(this.uniteId).subscribe({
      next: (points) => {
        this.points.set(points.filter((p) => p.statut === 'ACTIF'));
        this.network.affectations(this.uniteId).subscribe({
          next: (lignes) => {
            this.lignes.set(lignes);
            this.network.utilisateurs(this.uniteId).subscribe({
              next: (users) => {
                this.caissiers.set(users.filter((u) => u.role === 'CAISSIER' && u.statut === 'ACTIF'));
                this.loading.set(false);
              },
              error: () => this.echec('Caissiers indisponibles.')
            });
          },
          error: () => this.echec('Affectations indisponibles.')
        });
      },
      error: () => this.echec('Points de vente indisponibles.')
    });
  }

  private echec(message: string): void {
    this.loading.set(false);
    this.error.set(message);
  }

  private vide() {
    const jour = new Date().toISOString().slice(0, 10);
    return { pointId: this.points()[0]?.id ?? '', utilisateurId: '', debut: jour, fin: '' };
  }
}
