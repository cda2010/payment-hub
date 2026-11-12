import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { keycloak } from './keycloak';

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  fullName: string | null;
  roles: string[];
}

/** Lu par RapidFrame (`auth.tokenKey` dans app.config.ts) pour ses propres appels. */
const ACCESS_KEY = 'gw_access_token';
const LEGACY_KEYS = ['gw_refresh_token', 'gw_user'];
const TECHNICAL_ROLES = new Set(['offline_access', 'uma_authorization']);

/** Session Keycloak (OIDC code + PKCE). Le refresh token reste en mémoire (keycloak-js). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);
  private readonly userSignal = signal<AuthUser | null>(null);

  readonly user = this.userSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.userSignal());

  constructor() {
    LEGACY_KEYS.forEach((k) => localStorage.removeItem(k));
    keycloak.onAuthRefreshSuccess = () => this.sync();
    keycloak.onAuthLogout = () => this.clear();
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => this.clear());
    };
    this.sync();
  }

  /** Redirige vers la page de connexion Keycloak. */
  login(redirectPath = '/admin'): Promise<void> {
    return keycloak.login({ redirectUri: `${location.origin}${redirectPath}` });
  }

  logout(): void {
    this.clear();
    void keycloak.logout({ redirectUri: `${location.origin}/login` });
  }

  /** Session expirée côté API (401) : retour à l'écran de connexion sans appel Keycloak. */
  expireSession(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return keycloak.token ?? null;
  }

  /** Token rafraîchi s'il expire dans moins de 30 s ; null si pas de session. */
  async getValidAccessToken(): Promise<string | null> {
    if (!keycloak.authenticated) {
      return null;
    }
    try {
      if (await keycloak.updateToken(30)) {
        this.sync();
      }
      return keycloak.token ?? null;
    } catch {
      this.clear();
      return null;
    }
  }

  hasRole(...roles: string[]): boolean {
    const user = this.userSignal();
    if (!user) {
      return false;
    }
    return roles.some((role) => user.roles.includes(role));
  }

  private sync(): void {
    const t = keycloak.tokenParsed as Record<string, unknown> | undefined;
    if (!keycloak.authenticated || !keycloak.token || !t) {
      this.clear();
      return;
    }
    localStorage.setItem(ACCESS_KEY, keycloak.token);
    const roles = (Array.isArray(t['roles']) ? (t['roles'] as string[]) : []).filter(
      (r) => !TECHNICAL_ROLES.has(r) && !r.startsWith('default-roles-')
    );
    this.userSignal.set({
      id: String(t['sub'] ?? ''),
      username: String(t['preferred_username'] ?? t['sub'] ?? ''),
      email: String(t['email'] ?? ''),
      fullName: (t['name'] as string | undefined) ?? null,
      roles
    });
  }

  private clear(): void {
    localStorage.removeItem(ACCESS_KEY);
    this.userSignal.set(null);
  }
}
