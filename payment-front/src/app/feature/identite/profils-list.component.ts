import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import {
  AUDIENCES,
  IdentityAudience,
  IdentityService,
  Party,
  Profile,
  ProfileKind,
  SENS_LIMITE,
  SensLimite
} from '../../core/service/identity.service';
import { aujourdhui } from '../tarification/tarif-format';
import { ProfilFormDialogComponent } from './profil-form-dialog.component';

/** Catalogue des profils SERVICE, TARIFF ou LIMIT (selon la route). */
@Component({
  selector: 'app-profils-list',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    ProfilFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>{{ titre() }}</h1>
          <p class="subtitle">
            {{ sousTitre() }}
            @if (!loading()) {
              · <strong>{{ profils().length }}</strong> profil(s)
            }
          </p>
        </div>
        <div class="header-actions">
          @if (peutEcrire()) {
            <button pButton type="button" icon="pi pi-plus" [label]="labelCreer()" (click)="ouvrirForm(null)"></button>
          }
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="charger()">
        <p-dropdown
          name="audience"
          [options]="audiences"
          optionLabel="label"
          optionValue="value"
          placeholder="Audience"
          [showClear]="true"
          [(ngModel)]="filtreAudience"
          styleClass="filtre-dd"
        />
        <button pButton type="submit" icon="pi pi-search" label="Filtrer" [disabled]="loading()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <p-table [value]="profils()" [rowHover]="true" dataKey="id" styleClass="p-datatable-sm">
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Audience</th>
                <th class="num">{{ enteteNombre() }}</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-p>
              <tr [class.inactif]="!p.active">
                <td class="code">{{ p.code }}</td>
                <td>
                  <div>{{ p.libelle }}</div>
                  @if (p.description) {
                    <small class="muted">{{ p.description }}</small>
                  }
                </td>
                <td>{{ audienceLabel(p.audience) }}</td>
                <td class="num" [title]="detailCodes(p)">
                  {{ compte(p) }}
                </td>
                <td>
                  <p-tag [value]="p.active ? 'Actif' : 'Inactif'" [severity]="p.active ? 'success' : 'secondary'" />
                </td>
                <td class="actions">
                  @if (peutEcrire()) {
                    <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-pencil" title="Modifier" (click)="ouvrirForm(p)"></button>
                    <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-user-plus" title="Affecter à une party" (click)="ouvrirAffectation(p)"></button>
                  }
                  <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-eye" title="Détail" (click)="voir(p)"></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="6">Aucun profil.</td></tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <app-profil-form-dialog [(visible)]="formVisible" [profil]="edition" [kind]="kind" (saved)="apresSauve($event)" />

    <p-dialog
      [(visible)]="detailVisible"
      [header]="detail ? 'Profil ' + detail.code : 'Profil'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '40rem' }"
    >
      @if (detail; as p) {
        <p class="muted">{{ p.libelle }}@if (p.description) { — {{ p.description }} }</p>
        @if (p.kind === 'SERVICE') {
          <h3>Services</h3>
          <ul>
            @for (s of p.services; track s.serviceCode) {
              <li><code>{{ s.serviceCode }}</code> @if (s.libelle) { — {{ s.libelle }} }</li>
            } @empty {
              <li class="muted">Aucun</li>
            }
          </ul>
        }
        @if (p.kind === 'TARIFF') {
          <h3>Grilles</h3>
          <ul>
            @for (g of p.grilles; track g.produitCode + g.canalCode) {
              <li>
                <code>{{ g.produitCode }}</code> → <code>{{ g.grilleCode }}</code>
                @if (g.canalCode) {
                  · canal {{ g.canalCode }}
                }
                @if (g.devise) {
                  · {{ g.devise }}
                }
              </li>
            } @empty {
              <li class="muted">Aucune</li>
            }
          </ul>
        }
        @if (p.kind === 'LIMIT') {
          <h3>Limites</h3>
          <ul>
            @for (l of p.limites ?? []; track l.produitCode + l.sens) {
              <li>
                <code>{{ l.produitCode }}</code> · {{ sensLabel(l.sens) }} → <code>{{ l.groupeCode }}</code>
              </li>
            } @empty {
              <li class="muted">Aucune</li>
            }
          </ul>
        }
      }
    </p-dialog>

    <p-dialog
      [(visible)]="affVisible"
      header="Affecter le profil à une party"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '36rem' }"
    >
      @if (affProfil; as p) {
        <p class="muted">Profil <strong>{{ p.code }}</strong> — {{ p.libelle }}</p>
        <div class="gw-field">
          <label for="aff-party">Party *</label>
          <p-dropdown
            inputId="aff-party"
            [options]="parties()"
            optionLabel="affichage"
            dataKey="id"
            [(ngModel)]="affParty"
            [filter]="true"
            filterBy="affichage"
            placeholder="Choisir une party"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        <div class="gw-field">
          <label for="aff-debut">Début</label>
          <input pInputText type="date" id="aff-debut" [(ngModel)]="affDebut" name="affDebut" />
        </div>
        @if (affErreur()) {
          <p class="gw-msg error">{{ affErreur() }}</p>
        }
        <div class="dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="affVisible = false"></button>
          <button pButton type="button" label="Affecter" [loading]="affSaving()" (click)="affecter()"></button>
        </div>
      }
    </p-dialog>
  `,
  styles: `
    .filtres { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
    :host ::ng-deep .filtre-dd { min-width: 10rem; }
    .code { font-weight: 600; font-family: ui-monospace, monospace; }
    .num { text-align: right; }
    .muted { color: #64748b; font-size: 0.8rem; }
    .inactif { opacity: 0.65; }
    .actions { white-space: nowrap; text-align: right; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
    .gw-field { display: flex; flex-direction: column; gap: 0.25rem; margin-top: 0.75rem; }
    h3 { font-size: 0.95rem; margin: 0.75rem 0 0.35rem; }
    ul { margin: 0; padding-left: 1.2rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class ProfilsListComponent implements OnInit {
  private readonly identity = inject(IdentityService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly profils = signal<Profile[]>([]);
  readonly parties = signal<(Party & { affichage: string })[]>([]);
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER', 'OPERATOR'));
  readonly audiences = AUDIENCES;

  /** Forcé par l'action-view RapidFrame ; sinon lu sur la route /admin. */
  @Input() kindForce: ProfileKind | null = null;

  kind: ProfileKind = 'SERVICE';
  filtreAudience: IdentityAudience | null = null;
  formVisible = false;
  edition: Profile | null = null;
  detailVisible = false;
  detail: Profile | null = null;
  affVisible = false;
  affProfil: Profile | null = null;
  affParty: (Party & { affichage: string }) | null = null;
  affDebut = aujourdhui();
  readonly affSaving = signal(false);
  readonly affErreur = signal<string | null>(null);

  ngOnInit(): void {
    this.kind = this.kindForce ?? ((this.route.snapshot.data['profileKind'] as ProfileKind) || 'SERVICE');
    this.charger();
    this.identity.listerParties().subscribe({
      next: (list) =>
        this.parties.set(
          list.map((p) => ({ ...p, affichage: `${p.code} · ${p.displayName} (${p.kind})` }))
        )
    });
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
      return 'Grilles tarifaires de référence affectées aux identités / unités';
    }
    if (this.kind === 'LIMIT') {
      return 'Groupes de plafonds (produit et sens) affectés aux identités / unités';
    }
    return 'Services catalogue autorisés pour les identités / unités';
  }

  labelCreer(): string {
    if (this.kind === 'TARIFF') {
      return 'Nouveau profil tarifaire';
    }
    if (this.kind === 'LIMIT') {
      return 'Nouveau profil de limite';
    }
    return 'Nouveau profil service';
  }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.identity.listerProfils(null, this.filtreAudience, this.kind).subscribe({
      next: (list) => {
        this.profils.set(list);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  ouvrirForm(p: Profile | null): void {
    this.edition = p;
    this.formVisible = true;
  }

  apresSauve(_p: Profile): void {
    this.charger();
  }

  voir(p: Profile): void {
    this.detail = p;
    this.detailVisible = true;
  }

  ouvrirAffectation(p: Profile): void {
    this.affProfil = p;
    this.affParty = null;
    this.affDebut = aujourdhui();
    this.affErreur.set(null);
    this.affVisible = true;
  }

  affecter(): void {
    if (!this.affProfil || !this.affParty) {
      this.affErreur.set('Choisissez une party.');
      return;
    }
    this.affSaving.set(true);
    this.affErreur.set(null);
    this.identity
      .affecterProfil(this.affParty.id, { profileId: this.affProfil.id, dateDebut: this.affDebut || null })
      .subscribe({
        next: () => {
          this.affSaving.set(false);
          this.affVisible = false;
        },
        error: (e: { message?: string }) => {
          this.affSaving.set(false);
          this.affErreur.set(e?.message ?? 'Affectation impossible.');
        }
      });
  }

  audienceLabel(a: IdentityAudience | null | undefined): string {
    if (!a) {
      return '—';
    }
    return AUDIENCES.find((x) => x.value === a)?.label ?? a;
  }

  codesServices(p: Profile): string {
    return p.services.map((s) => s.serviceCode).join(', ');
  }

  codesGrilles(p: Profile): string {
    return p.grilles.map((g) => `${g.produitCode}→${g.grilleCode}`).join(', ');
  }

  enteteNombre(): string {
    if (this.kind === 'TARIFF') {
      return 'Grilles';
    }
    if (this.kind === 'LIMIT') {
      return 'Limites';
    }
    return 'Services';
  }

  compte(p: Profile): number {
    if (this.kind === 'TARIFF') {
      return p.grilles.length;
    }
    if (this.kind === 'LIMIT') {
      return (p.limites ?? []).length;
    }
    return p.services.length;
  }

  detailCodes(p: Profile): string {
    if (this.kind === 'TARIFF') {
      return this.codesGrilles(p);
    }
    if (this.kind === 'LIMIT') {
      return (p.limites ?? []).map((l) => `${l.produitCode}/${l.sens}→${l.groupeCode}`).join(', ');
    }
    return this.codesServices(p);
  }

  sensLabel(sens: SensLimite): string {
    return SENS_LIMITE.find((s) => s.value === sens)?.label ?? sens;
  }
}
