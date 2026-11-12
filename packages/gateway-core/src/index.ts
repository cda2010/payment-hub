/**
 * @payment2/gateway-core — client headless Product Gateway.
 * Aucune dépendance Angular / Ionic / PrimeNG / Form.io.
 */

export type {
  GatewayField,
  GatewayFieldOption,
  GatewayFieldType,
  GatewayProductField,
  GatewayUi,
  IntegrationScreen,
  IntegrationScreenAction,
  IntegrationScreenType,
  Payment2ClientDefaults,
  StartProductRequest
} from './types';

export {
  buildDataSourceQuery,
  dependsOnList,
  extractAmountChips,
  extractRfDataSource,
  extractRfShowWhen,
  isShowWhenVisible,
  parentsReady,
  type RfDataSourceConfig,
  type RfShowWhen
} from './rf-datasource';

export {
  displayHtmlFromIntegrationScreen,
  fieldsFromFormioComponents,
  fieldsFromIntegrationScreen,
  fieldsFromProductCollect,
  hiddenDefaultsFromIntegrationScreen,
  isPassThroughForm,
  mapGatewayField,
  mapGatewayFields,
  productJourneyHint,
  productNeedsLocalCollect,
  withoutPendingAmount
} from './form-mapper';

export {
  cloneScreen,
  isResumableStatus,
  screenPaymentId,
  screenStatus
} from './screen-meta';

export { Payment2Client } from './client';
