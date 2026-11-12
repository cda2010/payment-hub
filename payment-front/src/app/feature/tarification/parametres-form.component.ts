import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ParametreFormule } from '../../core/service/tarif.service';
import { SaisieParametres, estMontant, estTaux } from './tarif-format';

/** Formulaire généré à partir du schéma de paramètres d'une formule (cahier §5, écran D). */
@Component({
  selector: 'app-parametres-form',
  standalone: true,
  imports: [FormsModule, CheckboxModule, DropdownModule, InputTextModule],
  template: `
    @if (!schema.length) {
      <p class="aide">Cette formule n'a pas de paramètre.</p>
    }
    <div class="gw-form-grid">
      @for (p of tries(); track p.code) {
        <div class="gw-field">
          <label [for]="prefixe + p.code">
            {{ p.libelle }}{{ p.obligatoire ? ' *' : '' }}
            @if (suffixe(p); as s) {
              <span class="unite">({{ s }})</span>
            }
          </label>
          @if (p.typeDonnee === 'BOOLEAN') {
            <p-checkbox
              [binary]="true"
              [inputId]="prefixe + p.code"
              [disabled]="lectureSeule"
              [(ngModel)]="saisie[p.code]"
              (onChange)="changed.emit()"
            />
          } @else if (p.valeursAutorisees?.length) {
            <p-dropdown
              [inputId]="prefixe + p.code"
              [options]="p.valeursAutorisees!"
              [showClear]="!p.obligatoire"
              [disabled]="lectureSeule"
              [(ngModel)]="saisie[p.code]"
              (ngModelChange)="changed.emit()"
              placeholder="Choisir…"
              styleClass="w-full"
            />
          } @else {
            <input
              pInputText
              [id]="prefixe + p.code"
              [readonly]="lectureSeule"
              [class.ng-invalid]="!!erreurs[p.code]"
              [class.ng-dirty]="!!erreurs[p.code]"
              [attr.inputmode]="p.typeDonnee === 'STRING' ? null : 'decimal'"
              [placeholder]="exemple(p)"
              [(ngModel)]="saisie[p.code]"
              (ngModelChange)="changed.emit()"
            />
          }
          @if (erreurs[p.code]) {
            <small class="err">{{ erreurs[p.code] }}</small>
          } @else if (aide(p)) {
            <small class="aide">{{ aide(p) }}</small>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .gw-form-grid { align-items: start; }
    .unite { font-weight: 400; color: #94a3b8; margin-left: 0.25rem; }
    .err { color: #b91c1c; font-size: 0.78rem; }
    .aide { color: #64748b; font-size: 0.78rem; margin: 0; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class ParametresFormComponent {
  @Input({ required: true }) schema: ParametreFormule[] = [];
  /** Saisie brute, modifiée en place (taux en %). */
  @Input({ required: true }) saisie: SaisieParametres = {};
  /** code du paramètre → message (contrôles locaux et erreurs serveur). */
  @Input() erreurs: Record<string, string> = {};
  @Input() devise = '';
  @Input() lectureSeule = false;
  /** Distingue les identifiants de champs quand plusieurs formulaires sont à l'écran. */
  @Input() prefixe = 'param-';
  @Output() readonly changed = new EventEmitter<void>();

  tries(): ParametreFormule[] {
    return [...this.schema].sort((a, b) => a.ordre - b.ordre);
  }

  suffixe(p: ParametreFormule): string | null {
    if (estTaux(p)) {
      return '%';
    }
    if (estMontant(p)) {
      return this.devise || 'montant';
    }
    return p.unite && p.unite !== 'TAUX' && p.unite !== 'MONTANT' ? p.unite : null;
  }

  exemple(p: ParametreFormule): string {
    if (estTaux(p)) {
      return 'ex. 1,5';
    }
    return p.typeDonnee === 'INTEGER' ? 'entier' : p.typeDonnee === 'DECIMAL' ? 'ex. 100' : '';
  }

  /** L'aide du catalogue décrit la forme décimale stockée ; à l'écran le taux se saisit en %. */
  aide(p: ParametreFormule): string | null {
    const texte = (p.aide ?? '').replace(/Forme décimale[^.]*\./, '').trim();
    if (estTaux(p)) {
      return `${texte} Saisi en % : 1,5 pour 1,5 %.`.trim();
    }
    return texte || null;
  }
}
