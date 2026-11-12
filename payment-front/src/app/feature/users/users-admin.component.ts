import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputSwitchChangeEvent, InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { UserService } from '../../core/service/user.service';
import { AdminUser } from '../../core/model/api.models';

@Component({
  selector: 'app-users-admin',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    MultiSelectModule,
    InputSwitchModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Utilisateurs</h1>
          <p class="subtitle">Comptes et rôles d'accès au backoffice</p>
        </div>
        <div class="header-actions">
          <button
            pButton
            type="button"
            [label]="showForm() ? 'Fermer' : 'Nouvel utilisateur'"
            [class]="showForm() ? 'p-button-outlined' : ''"
            (click)="showForm.set(!showForm())"
          ></button>
        </div>
      </div>

      @if (showForm()) {
        <div class="gw-panel">
          <h2>Nouvel utilisateur</h2>
          <form [formGroup]="form" (ngSubmit)="create()" class="gw-form-grid">
            <div class="gw-field">
              <label for="username">Username</label>
              <input pInputText id="username" formControlName="username" class="w-full" />
            </div>
            <div class="gw-field">
              <label for="email">Email</label>
              <input pInputText id="email" type="email" formControlName="email" class="w-full" />
            </div>
            <div class="gw-field">
              <label for="password">Mot de passe</label>
              <input pInputText id="password" type="password" formControlName="password" class="w-full" />
            </div>
            <div class="gw-field">
              <label for="fullName">Nom complet</label>
              <input pInputText id="fullName" formControlName="fullName" class="w-full" />
            </div>
            <div class="gw-field full">
              <label for="roles">Rôles</label>
              <p-multiSelect
                inputId="roles"
                formControlName="roles"
                [options]="roles"
                placeholder="Sélectionner"
                display="chip"
                styleClass="w-full"
              />
            </div>
            <div class="gw-actions full">
              <button pButton type="submit" label="Créer" [disabled]="form.invalid"></button>
            </div>
          </form>
        </div>
      }

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <div class="gw-panel">
        <p-table
          [value]="users()"
          [lazy]="true"
          [loading]="loading()"
          [paginator]="true"
          [rows]="size()"
          [totalRecords]="total()"
          [first]="page() * size()"
          [rowsPerPageOptions]="[10, 20, 50]"
          (onLazyLoad)="onLazyLoad($event)"
          styleClass="p-datatable-sm"
          [tableStyle]="{ 'min-width': '48rem' }"
        >
          <ng-template pTemplate="header">
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Rôles</th>
              <th>Actif</th>
              <th>Dernière connexion</th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-u>
            <tr>
              <td>{{ u.username }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.roles.join(', ') }}</td>
              <td>
                <p-inputSwitch
                  [ngModel]="u.enabled"
                  [ngModelOptions]="{ standalone: true }"
                  (onChange)="onToggle(u, $event)"
                />
              </td>
              <td>{{ u.lastLoginAt ? (u.lastLoginAt | date:'short') : '—' }}</td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="5">Aucun utilisateur</td></tr>
          </ng-template>
        </p-table>
      </div>
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }
    </div>
  `,
  styles: `
    .w-full { width: 100%; }
    .full { grid-column: 1 / -1; }
  `
})
export class UsersAdminComponent {
  private readonly usersApi = inject(UserService);
  private readonly fb = inject(FormBuilder);

  readonly roles = ['ADMIN', 'CONFIG_MANAGER', 'OPERATOR', 'SUPPORT', 'AUDITOR'];

  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly users = signal<AdminUser[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    fullName: [''],
    roles: [['OPERATOR'] as string[], Validators.required]
  });

  reload(pageIndex: number): void {
    this.loading.set(true);
    this.page.set(pageIndex);
    this.usersApi.list(pageIndex, this.size()).subscribe({
      next: (page) => {
        this.users.set(page.content);
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

  create(): void {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue();
    this.usersApi
      .create({
        username: v.username,
        email: v.email,
        password: v.password,
        fullName: v.fullName || null,
        roles: v.roles
      })
      .subscribe({
        next: () => {
          this.showForm.set(false);
          this.msg.set('Utilisateur créé');
          this.form.reset({
            username: '',
            email: '',
            password: '',
            fullName: '',
            roles: ['OPERATOR']
          });
          this.reload(0);
        },
        error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
      });
  }

  onToggle(u: AdminUser, ev: InputSwitchChangeEvent): void {
    this.toggle(u, !!ev.checked);
  }

  toggle(u: AdminUser, enabled: boolean): void {
    this.usersApi.setEnabled(u.id, enabled).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
      },
      error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
    });
  }
}
