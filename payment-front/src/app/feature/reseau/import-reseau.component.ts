import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { FormatModele, NetworkService, ObjetImport, RapportImport } from '../../core/service/network.service';

interface Colonne {
  nom: string;
  obligatoire: boolean;
  aide: string;
}

const COLONNES: Record<ObjetImport, Colonne[]> = {
  unites: [
    { nom: 'code', obligatoire: true, aide: 'Identifiant unique (lettres, chiffres, _ ou -). Un code existant met l’unité à jour.' },
    { nom: 'libelle', obligatoire: true, aide: '150 caractères au plus.' },
    { nom: 'type', obligatoire: true, aide: 'PAYS, FSE, AGENCE, MASTER, MARCHAND, SOUS_DISTRIBUTEUR ou REGROUPEMENT (ou leur libellé).' },
    {
      nom: 'code_parent',
      obligatoire: false,
      aide: 'Code de l’unité de rattachement, existante ou présente dans le fichier (l’ordre des lignes est libre). Vide pour un pays. Un autre parent pour une unité existante la déplace avec sa branche.'
    },
    { nom: 'zone', obligatoire: false, aide: 'Obligatoire pour un pays ; sinon héritée du parent (si renseignée, elle doit correspondre).' },
    {
      nom: 'code_fse',
      obligatoire: false,
      aide: 'Obligatoire si type = FSE : code Omnicanal du fournisseur (ex. 0011). Interdit pour les autres types.'
    }
  ],
  membres: [
    { nom: 'code_regroupement', obligatoire: true, aide: 'Code du regroupement.' },
    { nom: 'code_membre', obligatoire: true, aide: 'Unité de la branche du regroupement ; elle apporte toute sa descendance.' }
  ]
};

/**
 * Import en masse du réseau depuis un fichier CSV ou Excel : unités d'organisation (création, renommage, déplacement)
 * ou membres de regroupements. Le contrôle vérifie tout le fichier sans rien écrire ; l'import est tout ou rien.
 */
@Component({
  selector: 'app-import-reseau',
  standalone: true,
  imports: [FormsModule, RouterLink, ButtonModule, SelectButtonModule, TableModule, TagModule],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Import en masse</h1>
          <p class="subtitle">
            Charger des milliers d’unités ou de membres de regroupements depuis un fichier CSV ou Excel (20 000 lignes,
            10 Mo au plus)
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-sitemap" label="Arbre du réseau" routerLink="/admin/reseau/arbre"></a>
        </div>
      </div>

      @if (!peutEcrire()) {
        <p class="gw-msg error">L’import est réservé aux administrateurs et gestionnaires de configuration.</p>
      } @else {
        <div class="gw-panel">
          <div class="ligne">
            <p-selectButton
              [options]="objets"
              optionLabel="label"
              optionValue="value"
              [(ngModel)]="objet"
              [allowEmpty]="false"
              (onChange)="changerObjet()"
            />
            <span class="espace"></span>
            <span class="muted">Modèle :</span>
            <button pButton type="button" class="p-button-outlined p-button-sm" icon="pi pi-file" label="CSV" (click)="telechargerModele('csv')"></button>
            <button pButton type="button" class="p-button-outlined p-button-sm" icon="pi pi-file-excel" label="Excel" (click)="telechargerModele('xlsx')"></button>
          </div>

          <table class="colonnes">
            <thead>
              <tr><th>Colonne</th><th>Contenu</th></tr>
            </thead>
            <tbody>
              @for (c of colonnes(); track c.nom) {
                <tr>
                  <td class="nowrap"><code>{{ c.nom }}</code> @if (c.obligatoire) { <span class="obl">*</span> }</td>
                  <td>{{ c.aide }}</td>
                </tr>
              }
            </tbody>
          </table>
          <p class="muted">
            Première ligne : les en-têtes (casse, accents et espaces indifférents). CSV séparé par « ; », « , » ou
            tabulation, en UTF-8 ou tel qu’enregistré par Excel. Excel : première feuille.
            @if (objet === 'unites') {
              Le type et la zone d’une unité existante ne changent pas ; un FSE ou un pays ne se déplace pas.
            } @else {
              Une unité déjà couverte par le regroupement (elle-même, un ascendant ou un descendant membre) est refusée.
            }
          </p>

          <div class="ligne fichier">
            <input #champ type="file" accept=".csv,.txt,.xlsx,.xls" (change)="choisirFichier(champ.files)" hidden />
            <button pButton type="button" class="p-button-outlined" icon="pi pi-paperclip" label="Choisir un fichier" (click)="champ.value = ''; champ.click()"></button>
            @if (fichier(); as f) {
              <span><strong>{{ f.name }}</strong> <span class="muted">({{ taille(f.size) }})</span></span>
            } @else {
              <span class="muted">Aucun fichier choisi</span>
            }
            <span class="espace"></span>
            <button
              pButton
              type="button"
              class="p-button-outlined"
              icon="pi pi-check-square"
              label="Contrôler"
              [disabled]="!fichier() || enCours()"
              [loading]="enCours() && essaiEnCours"
              (click)="lancer(true)"
            ></button>
            <button
              pButton
              type="button"
              icon="pi pi-upload"
              label="Importer"
              [disabled]="!pretAImporter() || enCours()"
              [loading]="enCours() && !essaiEnCours"
              (click)="lancer(false)"
            ></button>
          </div>
          <p class="muted">« Importer » s’active une fois le fichier contrôlé sans erreur ; rien n’est écrit tant qu’une ligne est en erreur.</p>
          @if (erreur()) {
            <p class="gw-msg error">{{ erreur() }}</p>
          }
        </div>

        @if (rapport(); as r) {
          <div class="gw-panel">
            <h3>
              @if (r.applique) {
                <i class="pi pi-check-circle ok"></i> Import appliqué
              } @else if (r.nbErreurs) {
                <i class="pi pi-times-circle ko"></i> {{ r.nbErreurs }} erreur(s) : rien n’a été écrit
              } @else {
                <i class="pi pi-check ok"></i> Contrôle réussi : le fichier peut être importé
              }
            </h3>
            <div class="compteurs">
              <p-tag severity="secondary" [value]="r.nbLignes + ' ligne(s) · ' + r.format" />
              <p-tag severity="success" [value]="r.nbCreations + ' création(s)'" />
              @if (r.objet === 'UNITES') {
                <p-tag severity="info" [value]="r.nbMisesAJour + ' renommage(s)'" />
                <p-tag severity="warning" [value]="r.nbDeplacements + ' déplacement(s)'" />
              }
              <p-tag severity="secondary" [value]="r.nbInchangees + ' inchangée(s)'" />
              @if (r.nbErreurs) {
                <p-tag severity="danger" [value]="r.nbErreurs + ' erreur(s)'" />
              }
              <span class="muted">{{ r.essai ? 'Contrôle' : 'Import' }} en {{ r.dureeMs }} ms</span>
            </div>

            @if (r.erreurs.length) {
              <div class="ligne">
                <span class="muted">
                  @if (r.erreurs.length < r.nbErreurs) {
                    Les {{ r.erreurs.length }} premières erreurs sont listées.
                  }
                </span>
                <span class="espace"></span>
                <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-download" label="Erreurs en CSV" (click)="exporterErreurs(r)"></button>
              </div>
              <p-table [value]="r.erreurs" styleClass="p-datatable-sm" [paginator]="r.erreurs.length > 25" [rows]="25">
                <ng-template pTemplate="header">
                  <tr>
                    <th pSortableColumn="ligne">Ligne <p-sortIcon field="ligne" /></th>
                    <th pSortableColumn="code">Code <p-sortIcon field="code" /></th>
                    <th>Colonne</th>
                    <th pSortableColumn="codeErreur">Erreur <p-sortIcon field="codeErreur" /></th>
                    <th>Détail</th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-e>
                  <tr>
                    <td>{{ e.ligne || '—' }}</td>
                    <td class="nowrap">{{ e.code }}</td>
                    <td>{{ e.champ }}</td>
                    <td class="nowrap"><code>{{ e.codeErreur }}</code></td>
                    <td>{{ e.message }}</td>
                  </tr>
                </ng-template>
              </p-table>
            }
          </div>
        }
      }
    </div>
  `,
  styles: `
    .ligne { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem; }
    .fichier { margin-top: 1rem; }
    .espace { flex: 1; }
    .muted { color: #64748b; font-size: 0.82rem; }
    .colonnes { width: 100%; border-collapse: collapse; margin: 1rem 0 0.5rem; font-size: 0.85rem; }
    .colonnes th { text-align: left; color: #475569; font-weight: 600; border-bottom: 1px solid #e2e8f0; padding: 0.35rem 0.5rem; }
    .colonnes td { border-bottom: 1px solid #f1f5f9; padding: 0.35rem 0.5rem; vertical-align: top; }
    .obl { color: #b91c1c; }
    .nowrap { white-space: nowrap; }
    .compteurs { display: flex; flex-wrap: wrap; align-items: center; gap: 0.4rem; margin-bottom: 0.75rem; }
    h3 i { margin-right: 0.3rem; }
    .ok { color: #16a34a; }
    .ko { color: #dc2626; }
  `
})
export class ImportReseauComponent {
  private readonly network = inject(NetworkService);
  private readonly auth = inject(AuthService);

  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly objets: { value: ObjetImport; label: string }[] = [
    { value: 'unites', label: 'Unités d’organisation' },
    { value: 'membres', label: 'Membres de regroupements' }
  ];
  readonly fichier = signal<File | null>(null);
  readonly rapport = signal<RapportImport | null>(null);
  readonly erreur = signal<string | null>(null);
  readonly enCours = signal(false);
  /** Fichier contrôlé sans erreur, tel quel. */
  readonly controle = signal<File | null>(null);
  readonly pretAImporter = computed(() => !!this.fichier() && this.controle() === this.fichier());
  readonly objetCourant = signal<ObjetImport>('unites');
  readonly colonnes = computed(() => COLONNES[this.objetCourant()]);

  objet: ObjetImport = 'unites';
  essaiEnCours = true;

  changerObjet(): void {
    this.objetCourant.set(this.objet);
    this.reinitialiser();
  }

  choisirFichier(fichiers: FileList | null): void {
    this.fichier.set(fichiers?.[0] ?? null);
    this.reinitialiser();
  }

  lancer(essai: boolean): void {
    const f = this.fichier();
    if (!f) {
      return;
    }
    if (!essai && !window.confirm(`Importer ${f.name} ? Toutes les lignes seront appliquées en une fois.`)) {
      return;
    }
    this.essaiEnCours = essai;
    this.enCours.set(true);
    this.erreur.set(null);
    this.network.importer(this.objet, f, essai).subscribe({
      next: (r) => {
        this.enCours.set(false);
        this.rapport.set(r);
        this.controle.set(essai && r.nbErreurs === 0 ? f : null);
      },
      error: (e: { message?: string }) => {
        this.enCours.set(false);
        this.rapport.set(null);
        this.controle.set(null);
        this.erreur.set(e?.message ?? 'Import impossible.');
      }
    });
  }

  telechargerModele(format: FormatModele): void {
    this.network.modele(this.objet, format).subscribe({
      next: (blob) => this.enregistrer(blob, `modele-${this.objet}.${format}`),
      error: (e: { message?: string }) => this.erreur.set(e?.message ?? 'Modèle indisponible.')
    });
  }

  exporterErreurs(r: RapportImport): void {
    const champ = (v: unknown) => `"${String(v ?? '').replace(/"/g, '""')}"`;
    const lignes = r.erreurs.map((e) => [e.ligne, e.code, e.champ, e.codeErreur, e.message].map(champ).join(';'));
    const csv = '\uFEFF' + ['ligne;code;colonne;erreur;detail', ...lignes].join('\r\n');
    this.enregistrer(new Blob([csv], { type: 'text/csv;charset=utf-8' }), `erreurs-import-${this.objet}.csv`);
  }

  taille(octets: number): string {
    return octets < 1024 * 1024 ? `${Math.ceil(octets / 1024)} Ko` : `${(octets / 1024 / 1024).toFixed(1)} Mo`;
  }

  private reinitialiser(): void {
    this.rapport.set(null);
    this.erreur.set(null);
    this.controle.set(null);
  }

  private enregistrer(blob: Blob, nom: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nom;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url));
  }
}
