export const environment = {
  production: false,
  /** Chemin relatif → proxy Angular (évite les erreurs CORS localhost / 127.0.0.1). */
  apiUrl: '/api/v1',
  /** Base RapidFrame (meta / designer / bpm catalog) — distinct du gateway /api/v1 */
  rapidframeApiUrl: '/api',
  /** Moteur de tarification (payment-pricing) : /grilles, /formules, /frais — routé vers le port 8095 par le proxy. */
  pricingApiUrl: '/api/v1',
  /** Réseau de distribution (payment-network) : unités d'organisation et regroupements — port 8096 via le proxy. */
  networkApiUrl: '/api/v1/network',
  /** Identité (payment-identity) : parties et profils d'offre — port 8094 via le proxy. */
  identityApiUrl: '/api/v1/identity',
  /** Alias de comptes (payment-alias) : Party / Org / Unité network — port 8097 via le proxy. */
  aliasApiUrl: '/api/v1/aliases',
  /** Commissionnement (payment-commission) : grilles / formules / simulations — port 8098 via le proxy. */
  commissionApiUrl: '/api/v1/commission',
  /** Moteur de règles (payment-rules) : plafonds / cumuls / simulate — port 8099 via le proxy. Distinct de soft-rules catalogue. */
  rulesApiUrl: '/api/v1/rules',
  rulesGroupsApiUrl: '/api/v1/rule-groups',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'payment',
    clientId: 'payment-front'
  }
};
