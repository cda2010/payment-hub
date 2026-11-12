import { Component, OnInit, inject } from '@angular/core';
import { AuthService } from '../core/auth/auth.service';

/** Route menu RapidFrame → déclenche le logout JWT gateway. */
@Component({
  selector: 'app-admin-logout',
  standalone: true,
  template: `<p class="msg">Déconnexion…</p>`,
  styles: `
    .msg {
      padding: 2rem;
      color: #64748b;
    }
  `,
})
export class AdminLogoutComponent implements OnInit {
  private readonly auth = inject(AuthService);

  ngOnInit(): void {
    this.auth.logout();
  }
}
