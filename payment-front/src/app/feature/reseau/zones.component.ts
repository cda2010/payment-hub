import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import { NetworkService, ZoneRef } from '../../core/service/network.service';
import { erreursParChamp, statutLabel, statutSeverity } from './reseau-format';

interface SaisieZone {
  code: string;
  libelle: string;
  devise: string;
}

/**
 * Référentiel des zones monétaires. Une zone se crée, se renomme et se désactive (une fois ses pays désactivés) ;
 * elle ne se supprime pas. Son code est définitif, sa devise se fige dès qu'elle contient une unité.
 */
@Component({
  selector: 'app-zones',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Zones</h1>
          <p class="subtitle">
            Zones monétaires du réseau : chaque pays, et toute sa branche, appartient à une zone et utilise sa devise
            @if (!loading()) {
              · <strong>{{ zones().length }}</strong> zone(s)
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-sitemap" label="Arbre du réseau" routerLink="/admin/reseau/arbre"></a>
          @if (peutEcrire()) {
            <button pButton type="button" icon="pi pi-plus" label="Nouvelle zone" (click)="ouvrir(null)"></button>
          }
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
          <p-table [value]="zones()" [rowHover]="true" dataKey="code" styleClass="p-datatable-sm">
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Devise</th>
                <th class="num">Pays</th>
                <th class="num">Unités</th>
                <th>Statut</th>
                <th>Mise à jour</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-z>
              <tr [class.inactif]="z.statut === 'INACTIF'">
                <td class="code">{{ z.code }}</td>
                <td>{{ z.libelle }}</td>
                <td><span class="devise">{{ z.devise }}</span></td>
                <td class="num">{{ z.nbPays }}</td>
                <td class="num">{{ z.nbUnites }}</td>
                <td><p-tag [value]="statut(z.statut)" [severity]="statutSev(z.statut)" /></td>
                <td class="muted">
                  {{ z.updatedAt | date: 'dd/MM/yyyy HH:mm' }}
                  @if (z.updatedBy) { · {{ z.updatedBy }} }
                </td>
                <td class="actions">
                  <a
                    pButton
                    class="p-button-text p-button-sm"
                    icon="pi pi-sitemap"
                    title="Voir l'arbre de la zone"
                    routerLink="/admin/reseau/arbre"
                    [queryParams]="{ zone: z.code }"
                  ></a>
                  @if (peutEcrire()) {
                    <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-pencil" title="Modifier" (click)="ouvrir(z)"></button>
                    @if (z.statut === 'ACTIF') {
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-sm p-button-danger"
                        icon="pi pi-ban"
                        title="Désactiver"
                        [loading]="enCours() === z.code"
                        (click)="changerStatut(z, false)"
                      ></button>
                    } @else {
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-sm p-button-success"
                        icon="pi pi-check"
                        title="Réactiver"
                        [loading]="enCours() === z.code"
                        (click)="changerStatut(z, true)"
                      ></button>
                    }
                  }
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="8">Aucune zone.</td></tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <p-dialog
      [(visible)]="dialogue"
      [header]="edition ? 'Modifier la zone ' + edition.code : 'Nouvelle zone'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '34rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label for="z-code">Code *</label>
          <input pInputText id="z-code" name="code" [(ngModel)]="s.code" placeholder="ex. EAC" maxlength="10" [readonly]="!!edition" />
          <small class="muted">{{ edition ? 'Le code ne change pas.' : '2 à 10 lettres, chiffres ou _ ; définitif.' }}</small>
          @if (err()['code']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="z-devise">Devise *</label>
          <input
            pInputText
            id="z-devise"
            name="devise"
            [(ngModel)]="s.devise"
            placeholder="ex. XOF"
            maxlength="3"
            [readonly]="deviseFigee()"
          />
          <small class="muted">
            {{ deviseFigee() ? 'Figée : la zone contient déjà des unités.' : 'Code ISO 4217 sur 3 lettres.' }}
          </small>
          @if (err()['devise']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field full">
          <label for="z-libelle">Libellé *</label>
          <input pInputText id="z-libelle" name="libelle" [(ngModel)]="s.libelle" maxlength="100" />
          @if (err()['libelle']; as e) { <small class="err">{{ e }}</small> }
        </div>
        @if (erreurDialogue()) {
          <p class="gw-msg error full">{{ erreurDialogue() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogue = false"></button>
          <button pButton type="submit" [label]="edition ? 'Enregistrer' : 'Créer'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .code { font-weight: 600; white-space: nowrap; }
    .devise { font-family: monospace; background: #f1f5f9; padding: 0.1rem 0.35rem; border-radius: 4px; }
    .num { text-align: right; }
    .muted { color: #64748b; font-size: 0.78rem; }
    .err { color: #b91c1c; font-size: 0.78rem; }
    .actions { white-space: nowrap; text-align: right; }
    tr.inactif td { opacity: 0.6; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
  `
})
export class ZonesComponent implements OnInit {
  private readonly network = inject(NetworkService);
  private readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly zones = signal<ZoneRef[]>([]);
  readonly enCours = signal<string | null>(null);
  readonly saving = signal(false);
  readonly erreurDialogue = signal<string | null>(null);
  readonly err = signal<Record<string, string>>({});
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));

  dialogue = false;
  edition: ZoneRef | null = null;
  s: SaisieZone = { code: '', libelle: '', devise: '' };

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.network.zones().subscribe({
      next: (z) => {
        this.zones.set(z);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  deviseFigee(): boolean {
    return !!this.edition && this.edition.nbUnites > 0;
  }

  ouvrir(z: ZoneRef | null): void {
    this.edition = z;
    this.s = z ? { code: z.code, libelle: z.libelle, devise: z.devise } : { code: '', libelle: '', devise: '' };
    this.err.set({});
    this.erreurDialogue.set(null);
    this.dialogue = true;
  }

  enregistrer(): void {
    const local: Record<string, string> = {};
    const code = this.s.code.trim().toUpperCase();
    const devise = this.s.devise.trim().toUpperCase();
    if (!this.edition && !/^[A-Z][A-Z0-9_]{1,9}$/.test(code)) {
      local['code'] = '2 à 10 lettres, chiffres ou _, commençant par une lettre.';
    }
    if (!/^[A-Z]{3}$/.test(devise)) local['devise'] = 'Code ISO 4217 sur 3 lettres.';
    if (!this.s.libelle.trim()) local['libelle'] = 'Obligatoire.';
    this.err.set(local);
    if (Object.keys(local).length) {
      return;
    }
    this.erreurDialogue.set(null);
    this.saving.set(true);
    const libelle = this.s.libelle.trim();
    const req = this.edition
      ? this.network.modifierZone(this.edition.code, { libelle, devise })
      : this.network.creerZone({ code, libelle, devise });
    req.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogue = false;
        this.charger();
      },
      error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
        this.saving.set(false);
        this.erreurDialogue.set(e?.message ?? 'Enregistrement impossible.');
        this.err.set(erreursParChamp(e?.details ?? []));
      }
    });
  }

  changerStatut(z: ZoneRef, activer: boolean): void {
    const question = activer
      ? `Réactiver la zone ${z.code} ?`
      : `Désactiver la zone ${z.code} ? Plus aucun pays ne pourra y être créé ni réactivé.`;
    if (!window.confirm(question)) {
      return;
    }
    this.enCours.set(z.code);
    this.error.set(null);
    (activer ? this.network.activerZone(z.code) : this.network.desactiverZone(z.code)).subscribe({
      next: () => {
        this.enCours.set(null);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.enCours.set(null);
        this.error.set(e?.message ?? 'Changement de statut impossible.');
      }
    });
  }

  statut = statutLabel;
  statutSev = statutSeverity;
}
