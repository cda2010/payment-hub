export const environment = {
  production: true,
  apiUrl: '/api/v1',
  rapidframeApiUrl: '/api',
  pricingApiUrl: '/api/v1',
  networkApiUrl: '/api/v1/network',
  identityApiUrl: '/api/v1/identity',
  aliasApiUrl: '/api/v1/aliases',
  commissionApiUrl: '/api/v1/commission',
  rulesApiUrl: '/api/v1/rules',
  rulesGroupsApiUrl: '/api/v1/rule-groups',
  /** URL publique de Keycloak en production (doit correspondre à l'issuer configuré côté backend). */
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'payment',
    clientId: 'payment-front'
  }
};
