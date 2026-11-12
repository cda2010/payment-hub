import { Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TagModule } from 'primeng/tag';
import { Correspondance, ParametreFormule, ResultatFrais } from '../../core/service/tarif.service';
import { montant, resumeParametres } from './tarif-format';

/** Détail d'un calcul de frais : grille et formule utilisées, tranche, étapes et formule développée (EF-009). */
@Component({
  selector: 'app-resultat-frais',
  standalone: true,
  imports: [DatePipe, TagModule],
  template: `
    <div class="total">
      <span class="lbl">Frais</span>
      <span class="val">{{ m(r.frais, r.devise) }}</span>
      @if (r.ajustement) {
        <p-tag [value]="r.ajustement === 'PLANCHER' ? 'Plancher appliqué' : 'Plafond appliqué'" severity="warning" />
      }
    </div>
    <code class="details">{{ r.details }}</code>
    <dl>
      <dt>Montant</dt>
      <dd>{{ m(r.montant, r.devise) }}</dd>
      <dt>Grille</dt>
      <dd>
        <strong>{{ r.grilleCode }}</strong> v{{ r.versionGrille }}
        <span class="muted">· calcul au {{ r.dateCalcul | date: 'dd/MM/yyyy' }}</span>
      </dd>
      @if (r.source; as c) {
        <dt>Source couverte</dt>
        <dd>{{ correspondance(c) }}</dd>
      }
      @if (r.destination; as c) {
        <dt>Destination couverte</dt>
        <dd>{{ correspondance(c) }}</dd>
      }
      <dt>Tranche</dt>
      <dd>
        n° {{ r.tranche.ordre }} : {{ m(r.tranche.montantMin) }} →
        {{ r.tranche.montantMax !== undefined && r.tranche.montantMax !== null ? m(r.tranche.montantMax) : '∞' }}
      </dd>
      <dt>Formule</dt>
      <dd>
        <code>{{ r.formuleCode }}</code> v{{ r.versionFormule }}
        @if (parametres) {
          <span class="muted">· {{ parametres }}</span>
        }
      </dd>
      <dt>Base taxable</dt>
      <dd>{{ m(r.baseTaxable, r.devise) }}</dd>
      <dt>Résultat de la formule</dt>
      <dd>{{ m(r.fraisFormule, r.devise) }}</dd>
      @if (r.ajustement) {
        <dt>Après {{ r.ajustement === 'PLANCHER' ? 'plancher' : 'plafond' }}</dt>
        <dd>
          {{ m(r.fraisAvantArrondi, r.devise) }}
          <span class="muted">
            (bornes de la tranche : {{ m(r.tranche.fraisMin) }} – {{ r.tranche.fraisMax !== undefined ? m(r.tranche.fraisMax) : '∞' }})
          </span>
        </dd>
      }
      <dt>Arrondi</dt>
      <dd>{{ r.arrondi.echelle }} décimale(s), {{ r.arrondi.mode }}</dd>
    </dl>
  `,
  styles: `
    .total { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.35rem; }
    .total .lbl { color: #64748b; font-size: 0.85rem; }
    .total .val { font-size: 1.5rem; font-weight: 700; color: var(--primary-color, #2563eb); }
    .details {
      display: block;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 0.4rem 0.6rem;
      margin-bottom: 0.75rem;
      font-size: 0.85rem;
      white-space: pre-wrap;
    }
    dl { display: grid; grid-template-columns: 11rem 1fr; gap: 0.35rem 0.75rem; margin: 0; font-size: 0.88rem; }
    dt { color: #64748b; }
    dd { margin: 0; }
    .muted { color: #94a3b8; }
  `
})
export class ResultatFraisComponent {
  @Input({ required: true }) r!: ResultatFrais;
  /** Schéma de la formule, pour présenter les paramètres (taux en %). */
  @Input() schema?: ParametreFormule[];

  get parametres(): string {
    return resumeParametres(this.schema, this.r.parametres ?? {}, this.r.devise);
  }

  correspondance(c: Correspondance): string {
    switch (c.niveau) {
      case 'ZONE':
        return `par la zone ${c.cible}`;
      case 'UNITE':
        return c.via ? `par le regroupement ${c.cible} (via ${c.via})` : `par l'unité ${c.cible}`;
      default:
        return 'grille sans restriction';
    }
  }

  m(v: number | null | undefined, devise?: string): string {
    return montant(v, devise);
  }
}
