import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { AuditService } from '../../core/service/audit.service';
import { AuditLog } from '../../core/model/api.models';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [DatePipe, TableModule],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Journal d'audit</h1>
          <p class="subtitle">Traçabilité des actions administrateur</p>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <div class="gw-panel">
        <p-table
          [value]="rows()"
          [lazy]="true"
          [loading]="loading()"
          [paginator]="true"
          [rows]="size()"
          [totalRecords]="total()"
          [first]="page() * size()"
          [rowsPerPageOptions]="[20, 50, 100]"
          (onLazyLoad)="onLazyLoad($event)"
          styleClass="p-datatable-sm"
          [tableStyle]="{ 'min-width': '56rem' }"
        >
          <ng-template pTemplate="header">
            <tr>
              <th>Date</th>
              <th>Acteur</th>
              <th>Action</th>
              <th>Ressource</th>
              <th>Détails</th>
              <th>IP</th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-a>
            <tr>
              <td>{{ a.createdAt | date:'medium' }}</td>
              <td>{{ a.actorUsername || '—' }}</td>
              <td>{{ a.action }}</td>
              <td>{{ a.resourceType }} {{ a.resourceId || '' }}</td>
              <td class="details">{{ a.details || '—' }}</td>
              <td>{{ a.ipAddress || '—' }}</td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="6">Aucun événement</td></tr>
          </ng-template>
        </p-table>
      </div>
    </div>
  `,
  styles: `
    .details {
      max-width: 320px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  `
})
export class AuditListComponent {
  private readonly audit = inject(AuditService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<AuditLog[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(50);

  reload(pageIndex: number): void {
    this.loading.set(true);
    this.page.set(pageIndex);
    this.audit.list(pageIndex, this.size()).subscribe({
      next: (page) => {
        this.rows.set(page.content);
        this.total.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Erreur');
        this.loading.set(false);
      }
    });
  }

  onLazyLoad(ev: TableLazyLoadEvent): void {
    const rows = ev.rows ?? this.size();
    const first = ev.first ?? 0;
    this.size.set(rows);
    this.reload(Math.floor(first / rows));
  }
}
