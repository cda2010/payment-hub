import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { RulesService, RuleGroupSummary } from '../../core/service/rules.service';

@Component({
  selector: 'app-groupe-detail',
  standalone: true,
  imports: [RouterLink, ProgressSpinnerModule, TableModule, TagModule],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (!groupe()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a routerLink="/admin/rules">Retour</a>
      } @else {
        @if (groupe(); as g) {
          <div class="gw-page-header">
            <div>
              <nav class="fil">
                <a routerLink="/admin/rules">Règles</a>
                <i class="pi pi-angle-right"></i>
                <span>{{ g.code }}</span>
              </nav>
              <h1>{{ g.code }}</h1>
              <p class="subtitle">{{ g.libelle }} · {{ g.evaluationStrategy }}</p>
            </div>
          </div>
          <div class="gw-panel">
            <h3>Règles du groupe ({{ g.items.length }})</h3>
            <p-table [value]="g.items" styleClass="p-datatable-sm">
              <ng-template pTemplate="header">
                <tr>
                  <th>Ordre</th>
                  <th>Code</th>
                  <th>Version</th>
                  <th>Message</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-i>
                <tr>
                  <td>{{ i.sortOrder }}</td>
                  <td class="code">{{ i.ruleCode }}</td>
                  <td>v{{ i.ruleVersion }}</td>
                  <td>{{ i.message }}</td>
                </tr>
              </ng-template>
            </p-table>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .fil { display: flex; gap: 0.35rem; align-items: center; font-size: 0.85rem; color: #64748b; margin-bottom: 0.35rem; }
    .fil a { color: var(--primary-color, #2563eb); text-decoration: none; }
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    h3 { margin: 0 0 0.75rem; font-size: 0.95rem; }
  `
})
export class GroupeDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(RulesService);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly groupe = signal<RuleGroupSummary | null>(null);

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');
    if (!code) {
      this.loading.set(false);
      this.error.set('Groupe introuvable.');
      return;
    }
    this.api.detailGroupe(code).subscribe({
      next: (g) => {
        this.groupe.set(g);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }
}
