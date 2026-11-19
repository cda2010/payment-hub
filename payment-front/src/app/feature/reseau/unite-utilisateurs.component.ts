import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { NetworkService, UtilisateurUnite } from '../../core/service/network.service';

const ROLES = [
  { value: 'ADMIN', label: 'Administrateur' },
  { value: 'CAISSIER', label: 'Caissier' }
];

const CANAUX = [
  { value: 'SMS', label: 'SMS' },
  { value: 'EMAIL', label: 'E-mail' }
];

const LOGIN = /^[A-Za-z][A-Za-z0-9._-]{2,39}$/;
const TELEPHONE = /^\+?[0-9]{8,15}$/;
const EMAIL = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

/** Utilisateurs d'une unité. Le login est dédié ; le mot de passe temporaire part par SMS ou e-mail. */
@Component({
  selector: 'app-unite-utilisateurs',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule
  ],
  template: `
    <div class="gw-panel">
      <div class="entete">
        <h3>Utilisateurs ({{ utilisateurs().length }})</h3>
        @if (peutEcrire) {
          <button pButton type="button" class="p-button-sm" icon="pi pi-plus" label="Ajouter un utilisateur" (click)="ouvrir()"></button>
        }
      </div>
      <p class="muted">Le login est choisi, ce n'est pas le téléphone. Une personne peut être rattachée à plusieurs unités, avec un rôle par unité. Le mot de passe n'est envoyé qu'à la création du compte.</p>
      @if (avis()) {
        <p class="gw-msg info">{{ avis() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        <p-table [value]="utilisateurs()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Login</th>
              <th>Nom</th>
              <th>Rôle</th>
              <th>Accès</th>
              <th>Statut</th>
              @if (peutEcrire) { <th></th> }
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-u>
            <tr>
              <td class="code">{{ u.identifiant }}</td>
              <td>{{ u.nom }}</td>
              <td><p-tag [value]="role(u.role)" [severity]="u.role === 'ADMIN' ? 'info' : 'secondary'" /></td>
              <td>{{ acces(u) }}</td>
              <td><p-tag [value]="u.statut === 'ACTIF' ? 'Actif' : 'Inactif'" [severity]="u.statut === 'ACTIF' ? 'success' : 'secondary'" /></td>
              @if (peutEcrire) {
                <td class="actions">
                  @if (u.statut === 'ACTIF') {
                    <button pButton type="button" class="p-button-text p-button-sm p-button-danger" icon="pi pi-ban" label="Désactiver" [loading]="enCours() === u.id" (click)="desactiver(u)"></button>
                  } @else {
                    <button pButton type="button" class="p-button-text p-button-sm p-button-success" icon="pi pi-check" label="Activer" [loading]="enCours() === u.id" (click)="activer(u)"></button>
                  }
                  <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-key" label="Renvoyer" (click)="ouvrirMotDePasse(u)"></button>
                </td>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td [attr.colspan]="peutEcrire ? 6 : 5">Aucun utilisateur sur cette unité.</td></tr>
          </ng-template>
        </p-table>
      }
    </div>

    <p-dialog
      [(visible)]="dialogue"
      header="Nouvel utilisateur de l'unité"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '32rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="creer()">
        <div class="gw-field">
          <label for="u-id">Login *</label>
          <input pInputText id="u-id" name="identifiant" [(ngModel)]="s.identifiant" maxlength="40" placeholder="ex. a.traore" />
        </div>
        <div class="gw-field">
          <label for="u-nom">Nom *</label>
          <input pInputText id="u-nom" name="nom" [(ngModel)]="s.nom" maxlength="150" />
        </div>
        <div class="gw-field">
          <label for="u-canal">Envoi *</label>
          <p-dropdown
            inputId="u-canal"
            name="canal"
            [options]="canaux"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.canal"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        @if (s.canal === 'SMS') {
          <div class="gw-field">
            <label for="u-tel">Téléphone *</label>
            <input pInputText id="u-tel" name="telephone" [(ngModel)]="s.telephone" maxlength="16" placeholder="22670123456" />
          </div>
        } @else {
          <div class="gw-field">
            <label for="u-mail">E-mail *</label>
            <input pInputText id="u-mail" name="email" [(ngModel)]="s.email" maxlength="180" placeholder="a.traore@coris.local" />
          </div>
        }
        <div class="gw-field full">
          <label for="u-role">Rôle *</label>
          <p-dropdown
            inputId="u-role"
            name="role"
            [options]="roles"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.role"
            [editable]="true"
            placeholder="ADMIN, CAISSIER, ou un autre code"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        @if (erreurDialogue()) {
          <p class="gw-msg error full">{{ erreurDialogue() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogue = false"></button>
          <button pButton type="submit" label="Créer" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>

    <p-dialog
      [(visible)]="dialogueMotDePasse"
      [header]="'Nouvel accès de ' + (cible?.nom ?? '')"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '28rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="reinitialiser()">
        <p class="muted full">Un mot de passe temporaire est envoyé. La personne le change à la première connexion.</p>
        @if (!cible?.canal) {
          <div class="gw-field">
            <label for="r-canal">Envoi *</label>
            <p-dropdown
              inputId="r-canal"
              name="canalReset"
              [options]="canaux"
              optionLabel="label"
              optionValue="value"
              [(ngModel)]="destination.canal"
              styleClass="w-full"
              appendTo="body"
            />
          </div>
          @if (destination.canal === 'SMS') {
            <div class="gw-field">
              <label for="r-tel">Téléphone *</label>
              <input pInputText id="r-tel" name="telephoneReset" [(ngModel)]="destination.telephone" maxlength="16" />
            </div>
          } @else {
            <div class="gw-field">
              <label for="r-mail">E-mail *</label>
              <input pInputText id="r-mail" name="emailReset" [(ngModel)]="destination.email" maxlength="180" />
            </div>
          }
        } @else {
          <p class="full">Destination : {{ acces(cible!) }}</p>
        }
        @if (erreurMotDePasse()) {
          <p class="gw-msg error full">{{ erreurMotDePasse() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogueMotDePasse = false"></button>
          <button pButton type="submit" label="Envoyer" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .entete { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .entete h3 { margin: 0; }
    .muted { color: #64748b; font-size: 0.8rem; margin: 0.35rem 0 0.75rem; }
    .gw-msg.info { color: #166534; }
    .code { font-weight: 600; font-family: ui-monospace, monospace; }
    .actions { text-align: right; white-space: nowrap; }
    .gw-loading.compact { padding: 0.75rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class UniteUtilisateursComponent implements OnChanges {
  private readonly network = inject(NetworkService);

  @Input({ required: true }) uniteId!: string;
  @Input() peutEcrire = false;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly avis = signal<string | null>(null);
  readonly utilisateurs = signal<UtilisateurUnite[]>([]);
  readonly saving = signal(false);
  readonly enCours = signal<string | null>(null);
  readonly erreurDialogue = signal<string | null>(null);
  readonly erreurMotDePasse = signal<string | null>(null);
  readonly roles = ROLES;
  readonly canaux = CANAUX;

  dialogue = false;
  dialogueMotDePasse = false;
  s = this.vide();
  cible: UtilisateurUnite | null = null;
  destination = { canal: 'SMS' as 'SMS' | 'EMAIL', telephone: '', email: '' };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['uniteId'] && this.uniteId) {
      this.charger();
    }
  }

  ouvrir(): void {
    this.s = this.vide();
    this.erreurDialogue.set(null);
    this.dialogue = true;
  }

  creer(): void {
    const identifiant = this.s.identifiant.trim();
    const nom = this.s.nom.trim();
    const role = this.s.role.trim();
    const canal = this.s.canal;
    const telephone = this.s.telephone.replace(/[\s.]/g, '');
    const email = this.s.email.trim();
    if (!LOGIN.test(identifiant) || !nom || !role) {
      this.erreurDialogue.set('Le login commence par une lettre. Le nom et le rôle sont obligatoires.');
      return;
    }
    if (canal === 'SMS' && !TELEPHONE.test(telephone)) {
      this.erreurDialogue.set('Le téléphone compte 8 à 15 chiffres.');
      return;
    }
    if (canal === 'EMAIL' && !EMAIL.test(email)) {
      this.erreurDialogue.set("L'adresse e-mail n'est pas valide.");
      return;
    }
    this.saving.set(true);
    this.erreurDialogue.set(null);
    this.network.creerUtilisateur(this.uniteId, {
      identifiant,
      nom,
      role,
      canal,
      telephone: canal === 'SMS' ? telephone : undefined,
      email: canal === 'EMAIL' ? email : undefined
    }).subscribe({
      next: (cree) => {
        this.saving.set(false);
        this.dialogue = false;
        this.avis.set(cree.nouveauCompte === false
          ? `${cree.identifiant} est rattaché à cette unité. Le mot de passe déjà connu est conservé.`
          : `Mot de passe temporaire envoyé par ${cree.canal === 'EMAIL' ? 'e-mail' : 'SMS'} à ${this.acces(cree)}. Il sera changé à la première connexion.`);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreurDialogue.set(e?.message ?? 'Création impossible.');
      }
    });
  }

  activer(u: UtilisateurUnite): void {
    this.enCours.set(u.id);
    this.error.set(null);
    this.network.activerUtilisateur(this.uniteId, u.id).subscribe({
      next: () => {
        this.enCours.set(null);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.enCours.set(null);
        this.error.set(e?.message ?? 'Activation impossible.');
      }
    });
  }

  desactiver(u: UtilisateurUnite): void {
    this.enCours.set(u.id);
    this.error.set(null);
    this.network.desactiverUtilisateur(this.uniteId, u.id).subscribe({
      next: () => {
        this.enCours.set(null);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.enCours.set(null);
        this.error.set(e?.message ?? 'Désactivation impossible.');
      }
    });
  }

  ouvrirMotDePasse(u: UtilisateurUnite): void {
    this.cible = u;
    this.destination = { canal: u.canal === 'EMAIL' ? 'EMAIL' : 'SMS', telephone: '', email: '' };
    this.erreurMotDePasse.set(null);
    this.dialogueMotDePasse = true;
  }

  reinitialiser(): void {
    if (!this.cible) {
      return;
    }
    const body: { canal?: string; telephone?: string; email?: string } = {};
    if (!this.cible.canal) {
      const telephone = this.destination.telephone.replace(/[\s.]/g, '');
      const email = this.destination.email.trim();
      if (this.destination.canal === 'SMS' && !TELEPHONE.test(telephone)) {
        this.erreurMotDePasse.set('Le téléphone compte 8 à 15 chiffres.');
        return;
      }
      if (this.destination.canal === 'EMAIL' && !EMAIL.test(email)) {
        this.erreurMotDePasse.set("L'adresse e-mail n'est pas valide.");
        return;
      }
      body.canal = this.destination.canal;
      body.telephone = this.destination.canal === 'SMS' ? telephone : undefined;
      body.email = this.destination.canal === 'EMAIL' ? email : undefined;
    }
    this.saving.set(true);
    this.erreurMotDePasse.set(null);
    this.network.reinitialiserMotDePasse(this.uniteId, this.cible.id, body).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogueMotDePasse = false;
        const ou = this.cible?.canal ? this.acces(this.cible) : (body.telephone || body.email || '');
        this.avis.set(`Mot de passe temporaire envoyé à ${ou}. Il sera changé à la première connexion.`);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreurMotDePasse.set(e?.message ?? 'Envoi impossible.');
      }
    });
  }

  role(code: string): string {
    return ROLES.find((r) => r.value === code)?.label ?? code;
  }

  acces(u: UtilisateurUnite): string {
    if (u.canal === 'EMAIL' && u.email) {
      return u.email;
    }
    if (u.canal === 'SMS' && u.telephone) {
      return u.telephone;
    }
    return '—';
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.network.utilisateurs(this.uniteId).subscribe({
      next: (list) => {
        this.utilisateurs.set(list);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.utilisateurs.set([]);
        this.error.set(e?.message ?? 'Utilisateurs indisponibles.');
      }
    });
  }

  private vide() {
    return { identifiant: '', nom: '', role: 'CAISSIER', canal: 'SMS' as 'SMS' | 'EMAIL', telephone: '', email: '' };
  }
}
