import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import {
  NetworkService,
  Referentiel,
  TypeRef,
  TypeUnite,
  UniteDetail,
  UniteResume,
  Zone
} from '../../core/service/network.service';
import { ChoixUniteComponent } from './choix-unite.component';
import { erreursParChamp, optionsZones, typeLabel } from './reseau-format';

interface Saisie {
  code: string;
  libelle: string;
  type: TypeUnite | null;
  zone: Zone | null;
  parentId: string | null;
  codeFse: string;
}

interface Option {
  value: string;
  label: string;
}

/**
 * Création d'une unité (regroupement compris) et modification du libellé ou du type.
 * Seuls les parents autorisés par les règles sont proposés ; le serveur reste juge.
 */
@Component({
  selector: 'app-unite-form-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, DropdownModule, InputTextModule, ChoixUniteComponent],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="titre()"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '42rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label for="u-code">Code *</label>
          <input
            pInputText
            id="u-code"
            name="code"
            [(ngModel)]="s.code"
            [placeholder]="s.type === 'PAYS' ? 'ex. GH' : 'ex. AG-DORI'"
            [maxlength]="s.type === 'PAYS' ? 2 : 40"
            [readonly]="!!unite"
          />
          @if (unite) {
            <small class="muted">Le code ne change pas.</small>
          } @else if (s.type === 'PAYS') {
            <small class="muted">Code ISO 3166 du pays, sur 2 lettres.</small>
          }
          @if (err()['code']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="u-libelle">Libellé *</label>
          <input pInputText id="u-libelle" name="libelle" [(ngModel)]="s.libelle" maxlength="150" />
          @if (err()['libelle']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="u-type">Type *</label>
          <p-dropdown
            inputId="u-type"
            name="type"
            [options]="typesProposes()"
            optionLabel="label"
            optionValue="value"
            placeholder="Choisir un type"
            [(ngModel)]="s.type"
            (onChange)="typeChange()"
            [disabled]="typesProposes().length < 2 && !!s.type"
            styleClass="w-full"
            appendTo="body"
          />
          @if (regle(); as r) { <small class="muted">{{ r }}</small> }
          @if (err()['type']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="u-zone">Zone {{ zoneObligatoire() ? '*' : '' }}</label>
          <p-dropdown
            inputId="u-zone"
            name="zone"
            [options]="zones()"
            optionLabel="label"
            optionValue="value"
            placeholder="Choisir une zone"
            emptyMessage="Aucune zone active"
            [(ngModel)]="s.zone"
            (onChange)="zoneChange()"
            [disabled]="zoneFigee()"
            styleClass="w-full"
            appendTo="body"
          />
          @if (zoneFigee() && !unite) { <small class="muted">Héritée du parent.</small> }
        </div>

        @if (avecParent()) {
          <div class="gw-field full">
            <label for="u-parent">Rattaché à *</label>
            @if (parent) {
              <input pInputText id="u-parent" [value]="parent.code + ' — ' + parent.libelle" readonly class="w-full" />
            } @else {
              <app-choix-unite
                inputId="u-parent"
                [zone]="s.zone"
                [types]="typeRef()?.parentsAutorises ?? []"
                [valeur]="parentChoisi"
                (valeurChange)="parentChoisi = $event; s.parentId = $event?.id ?? null"
                [placeholder]="s.zone ? 'Rechercher : ' + typesParents() : 'Choisissez d’abord la zone'"
                [disabled]="!s.zone"
                vide="Aucune unité active de ce type dans la zone"
              />
            }
            @if (err()['parentId']; as e) { <small class="err">{{ e }}</small> }
          </div>
        }

        @if (s.type === 'FSE') {
          <div class="gw-field">
            <label for="u-code-fse">Code FSE *</label>
            <input
              pInputText
              id="u-code-fse"
              name="codeFse"
              [(ngModel)]="s.codeFse"
              placeholder="ex. 0011"
              maxlength="20"
              [readonly]="!!unite"
            />
            @if (unite) {
              <small class="muted">Le code FSE Omnicanal ne change pas.</small>
            } @else {
              <small class="muted">Code Omnicanal du fournisseur (ex. 0011 Coris Money).</small>
            }
            @if (err()['codeFse']; as e) { <small class="err">{{ e }}</small> }
          </div>
        }

        @if (erreur()) {
          <p class="gw-msg error full">{{ erreur() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="unite ? 'Enregistrer' : 'Créer'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .err { color: #b91c1c; font-size: 0.78rem; }
    .muted { color: #64748b; font-size: 0.78rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class UniteFormDialogComponent implements OnChanges {
  private readonly network = inject(NetworkService);

  @Input() visible = false;
  /** Unité modifiée ; null : création. */
  @Input() unite: UniteDetail | null = null;
  /** Création sous cette unité (parent imposé). */
  @Input() parent: UniteDetail | null = null;
  /** Création : type proposé par défaut (ex. REGROUPEMENT depuis la liste des regroupements). */
  @Input() typeInitial: TypeUnite | null = null;
  @Input() zoneInitiale: Zone | null = null;
  @Output() readonly visibleChange = new EventEmitter<boolean>();
  @Output() readonly saved = new EventEmitter<UniteDetail>();

  readonly zones = signal<Option[]>([]);
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly err = signal<Record<string, string>>({});
  readonly referentiel = signal<Referentiel | null>(null);
  readonly typesProposes = signal<{ value: TypeUnite; label: string }[]>([]);
  s: Saisie = this.vide();
  parentChoisi: UniteResume | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      this.erreur.set(null);
      this.err.set({});
      this.s = this.unite ? this.depuis(this.unite) : this.vide();
      this.parentChoisi = null;
      this.network.referentiel().subscribe({
        next: (r) => {
          this.referentiel.set(r);
          // Zone saisie librement (création hors fiche parent) : seules les zones actives sont proposées.
          this.zones.set(optionsZones(r.zones, !this.zoneFigee()));
          if (!this.s.zone) {
            this.s.zone = r.zones.find((z) => z.statut === 'ACTIF')?.code ?? null;
          }
          this.majTypes();
        },
        error: (e: { message?: string }) => this.erreur.set(e?.message ?? 'Référentiel indisponible.')
      });
    }
  }

  titre(): string {
    if (this.unite) {
      return `Modifier ${this.unite.code}`;
    }
    if (this.parent) {
      return `Nouvelle unité sous ${this.parent.code}`;
    }
    if (this.typeInitial === 'PAYS') {
      return 'Nouveau pays';
    }
    return this.typeInitial === 'REGROUPEMENT' ? 'Nouveau regroupement' : 'Nouvelle unité d’organisation';
  }

  typeRef(): TypeRef | undefined {
    return this.referentiel()?.types.find((t) => t.code === this.s.type);
  }

  regle(): string | null {
    return this.typeRef()?.regle ?? null;
  }

  avecParent(): boolean {
    const t = this.typeRef();
    return !this.unite && !!t && !t.racine;
  }

  zoneObligatoire(): boolean {
    const t = this.typeRef();
    return !!t && t.racine;
  }

  zoneFigee(): boolean {
    return !!this.unite || !!this.parent;
  }

  typesParents(): string {
    return (this.typeRef()?.parentsAutorises ?? []).map(typeLabel).join(', ').toLowerCase();
  }

  typeChange(): void {
    this.s.parentId = this.parent?.id ?? null;
    this.parentChoisi = null;
  }

  zoneChange(): void {
    this.s.parentId = this.parent?.id ?? null;
    this.parentChoisi = null;
  }

  enregistrer(): void {
    const local: Record<string, string> = {};
    if (!this.unite && this.s.type === 'PAYS' && !/^[A-Za-z]{2}$/.test(this.s.code.trim())) {
      local['code'] = 'Code ISO du pays : 2 lettres (ex. GH).';
    } else if (!this.unite && !/^[A-Za-z0-9_-]{1,40}$/.test(this.s.code.trim())) {
      local['code'] = 'Lettres, chiffres, _ ou - (40 max).';
    }
    if (!this.s.libelle.trim()) local['libelle'] = 'Obligatoire.';
    if (!this.s.type) local['type'] = 'Obligatoire.';
    if (this.avecParent() && !this.s.parentId) local['parentId'] = 'Choisissez l’unité de rattachement.';
    if (!this.unite && this.s.type === 'FSE' && !/^[A-Za-z0-9_-]{1,20}$/.test(this.s.codeFse.trim())) {
      local['codeFse'] = 'Code FSE Omnicanal obligatoire (lettres, chiffres, _ ou -, 20 max).';
    }
    if (!this.unite && this.zoneObligatoire() && !this.s.zone) {
      this.erreur.set('La zone est obligatoire.');
    }
    this.err.set(local);
    if (Object.keys(local).length || (!this.unite && this.zoneObligatoire() && !this.s.zone)) {
      return;
    }
    this.erreur.set(null);
    this.saving.set(true);
    const type = this.s.type!;
    const req = this.unite
      ? this.network.modifier(this.unite.id, {
          libelle: this.s.libelle.trim(),
          type
        })
      : this.network.creer({
          code: this.s.code.trim().toUpperCase(),
          libelle: this.s.libelle.trim(),
          type,
          zone: this.s.zone,
          parentId: this.avecParent() ? this.s.parentId : null,
          codeFse: type === 'FSE' ? this.s.codeFse.trim().toUpperCase() : null
        });
    req.subscribe({
      next: (u) => {
        this.saving.set(false);
        this.saved.emit(u);
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Enregistrement impossible.');
        this.err.set(erreursParChamp(e?.details ?? []));
      }
    });
  }

  /**
   * Types proposés selon le contexte : enfants autorisés du parent imposé, types compatibles en modification,
   * tous les types sinon (un pays se crée à la racine de sa zone).
   */
  private majTypes(): void {
    const ref = this.referentiel();
    if (!ref) {
      return;
    }
    let types: TypeRef[];
    if (this.unite) {
      const actuel = ref.types.find((t) => t.code === this.unite!.type)!;
      if (actuel.racine || actuel.regroupement) {
        types = [actuel];
      } else {
        const typeParent = this.unite.parent?.type;
        types = ref.types.filter(
          (t) => !t.racine && !t.regroupement && (!typeParent || t.parentsAutorises.includes(typeParent))
        );
      }
    } else if (this.parent) {
      types = ref.types.filter((t) => this.parent!.typesEnfantsAutorises.includes(t.code));
    } else {
      types = ref.types;
    }
    this.typesProposes.set(types.map((t) => ({ value: t.code, label: t.libelle })));
    if (!this.s.type && types.length === 1) {
      this.s.type = types[0].code;
    }
  }

  private vide(): Saisie {
    return {
      code: '',
      libelle: '',
      type: this.typeInitial,
      zone: this.parent?.zone ?? this.zoneInitiale ?? null,
      parentId: this.parent?.id ?? null,
      codeFse: ''
    };
  }

  private depuis(u: UniteDetail): Saisie {
    return {
      code: u.code,
      libelle: u.libelle,
      type: u.type,
      zone: u.zone,
      parentId: u.parent?.id ?? null,
      codeFse: u.codeFse ?? ''
    };
  }
}
