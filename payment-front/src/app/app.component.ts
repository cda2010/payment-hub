import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { NavigationEnd, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { RapidFrameModule } from '@rapidframe/frontend-angular';
import { AuthService } from './core/auth/auth.service';

/**
 * Pattern paymentHub : rf-shell pour admin + produits (hub + parcours).
 * Login hors shell. Champs de formulaire restent PrimeNG dans un cadre rf-panel.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RapidFrameModule],
  template: `
    @if (useRfShell()) {
      <rf-shell [user]="rfUser()" [showBreadcrumb]="false" />
    } @else {
      <router-outlet />
    }
  `,
  styles: `
    :host {
      display: block;
      min-height: 100vh;
    }
  `,
})
export class AppComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private sub?: Subscription;

  private readonly url = signal(this.router.url);

  readonly useRfShell = computed(() => !isPublicSurface(this.url()));

  readonly rfUser = computed(() => {
    const u = this.auth.user();
    if (!u) {
      return undefined;
    }
    return { name: u.fullName || u.username };
  });

  ngOnInit(): void {
    this.sub = this.router.events.subscribe((e) => {
      if (e instanceof NavigationStart) {
        this.url.set(e.url);
      } else if (e instanceof NavigationEnd) {
        this.url.set(e.urlAfterRedirects || e.url);
        this.guardAdminSurface();
      }
    });
    this.guardAdminSurface();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private guardAdminSurface(): void {
    if (!this.useRfShell() || this.auth.isAuthenticated()) {
      return;
    }
    void this.router.navigate(['/login']);
  }
}

function isPublicSurface(url: string): boolean {
  const path = (url.split('?')[0] || '/').replace(/\/+$/, '') || '/';
  return path === '/login';
}
