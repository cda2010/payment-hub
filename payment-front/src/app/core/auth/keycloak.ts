import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

export const keycloak = new Keycloak({
  url: environment.keycloak.url,
  realm: environment.keycloak.realm,
  clientId: environment.keycloak.clientId
});

/**
 * Appelé avant le bootstrap Angular : traite le retour de Keycloak (code PKCE) ou
 * vérifie silencieusement une session SSO existante, sans forcer la connexion
 * (le checkout public reste accessible).
 */
export async function initKeycloak(): Promise<void> {
  try {
    await keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      silentCheckSsoRedirectUri: `${location.origin}/silent-check-sso.html`,
      checkLoginIframe: false
    });
  } catch (err) {
    console.error('[auth] Keycloak injoignable', err);
  }
}
