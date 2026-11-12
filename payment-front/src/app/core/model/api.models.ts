export type ExecutionMode = 'OTP' | 'REDIRECT' | 'PUSH' | 'USSD' | 'DIRECT' | 'ASYNC';
export type PaymentMethodType = 'MOBILE_MONEY' | 'CARD' | 'BANK_TRANSFER' | 'OTHER';
export type ServiceTypeCode =
  | 'DEFAUT'
  | 'LINK'
  | 'UNLINK'
  | 'CONSULTATION_SOLDE'
  | 'CONSULTATIONHIST'
  | 'TRANSFERT'
  | 'PAYEMENT'
  | 'PAIEMENT_FACTURE'
  | 'INFOCLIENT';

/** Liste admin / formulaires — DEFAUT en tête pour les produits sans famille précise. */
export const SERVICE_TYPE_CODES: ServiceTypeCode[] = [
  'DEFAUT',
  'TRANSFERT',
  'PAYEMENT',
  'PAIEMENT_FACTURE',
  'CONSULTATION_SOLDE',
  'CONSULTATIONHIST',
  'LINK',
  'UNLINK',
  'INFOCLIENT'
];

export type ServiceTypeCategory = 'ACCOUNT' | 'INQUIRY' | 'TRANSFER' | 'PAYMENT' | 'BILLING' | 'OTHER';
export type AuthType =
  | 'NONE'
  | 'BASIC'
  | 'API_KEY'
  | 'BEARER_TOKEN'
  | 'OAUTH2_CLIENT_CREDENTIALS'
  | 'CUSTOM_HEADER'
  | 'MTLS';
export type PaymentStatus =
  | 'CREATED'
  | 'INITIATED'
  | 'AWAITING_USER_INPUT'
  | 'OTP_GENERATION_PENDING'
  | 'OTP_REQUIRED'
  | 'PROCESSING'
  | 'REDIRECT_REQUIRED'
  | 'WAITING_CALLBACK'
  | 'WAITING_APPROVAL'
  | 'RETURNED_FOR_CORRECTION'
  | 'PENDING'
  | 'SUCCESS'
  | 'FAILED'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'UNKNOWN';
export type NextActionType =
  | 'FORM'
  | 'ENTER_OTP'
  | 'REDIRECT'
  | 'WAIT'
  | 'DISPLAY_MESSAGE'
  | 'SUCCESS'
  | 'FAILURE';
export type StepType =
  | 'START'
  | 'FORM'
  | 'API_CALL'
  | 'OTP'
  | 'ENTER_OTP'
  | 'REDIRECT'
  | 'WAIT_CALLBACK'
  | 'CHECK_STATUS'
  | 'SUCCESS'
  | 'FAILURE';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface Provider {
  id: string;
  code: string;
  libelle: string;
  description: string | null;
  logoUrl: string | null;
  actif: boolean;
  ordreAffichage: number | null;
  paymentMethodType: PaymentMethodType;
  modeExecution: ExecutionMode;
  serviceTypes: ServiceTypeCode[];
  codeFse?: string | null;
  codeExterne?: string | null;
  groupCode?: string | null;
  countryCode?: string | null;
  participantKind?: FseParticipantKind | null;
  fseStatut?: FseStatut | null;
  createdAt: string;
  updatedAt: string;
}

export type FseParticipantKind = 'WALLET' | 'BANQUE' | 'FACTURIER' | 'REMITTANCE' | 'RESEAU';
export type FseStatut = 'BANQUE' | 'NONBANQUE' | 'AUTRE';

export interface ServiceType {
  code: ServiceTypeCode;
  libelle: string;
  category: ServiceTypeCategory;
  description: string | null;
  actif: boolean;
  ordreAffichage: number | null;
}

export interface ProviderRequest {
  code: string;
  libelle: string;
  description?: string | null;
  logoUrl?: string | null;
  actif: boolean;
  ordreAffichage?: number | null;
  paymentMethodType: PaymentMethodType;
  modeExecution: ExecutionMode;
  serviceTypes?: ServiceTypeCode[];
  codeFse?: string | null;
  codeExterne?: string | null;
  groupCode?: string | null;
  countryCode?: string | null;
  participantKind?: FseParticipantKind | null;
  fseStatut?: FseStatut | null;
}

export interface ProviderTemplate {
  code: string;
  libelle: string;
  description: string;
  paymentMethodType: PaymentMethodType;
  modeExecution: ExecutionMode;
  defaultBaseUrl: string;
}

export interface CreateProviderFromTemplateRequest {
  templateCode: string;
  code: string;
  libelle: string;
  description?: string | null;
  environmentCode: string;
  baseUrl: string;
  actif: boolean;
  ordreAffichage?: number | null;
}

export interface ProviderConfigExport {
  version: number;
  exportedAt: string;
  provider: {
    code: string;
    libelle: string;
    description: string | null;
    logoUrl: string | null;
    actif: boolean;
    ordreAffichage: number | null;
    paymentMethodType?: PaymentMethodType;
    modeExecution: ExecutionMode;
    serviceTypes?: ServiceTypeCode[];
    codeFse?: string | null;
    codeExterne?: string | null;
    groupCode?: string | null;
    countryCode?: string | null;
    participantKind?: FseParticipantKind | null;
    fseStatut?: FseStatut | null;
  };
  environments: Array<{
    code: string;
    baseUrl: string;
    timeoutSeconds: number | null;
    actif: boolean;
  }>;
  authentications: Array<{
    environmentCode: string;
    authType: AuthType;
    clientId: string | null;
    tokenUrl: string | null;
    customHeaderName: string | null;
    extraConfigJson: string | null;
  }>;
  fseApis?: Array<{
    code: string;
    nom: string;
    typeApi: string;
    baseUrl: string;
    host: string | null;
    port: string | null;
    useSsl: boolean;
    useProxy: boolean;
    environmentCode: string | null;
    actif: boolean;
    authType: AuthType | null;
    clientId: string | null;
    tokenUrl: string | null;
    customHeaderName: string | null;
    extraConfigJson: string | null;
  }>;
  operations: Array<{
    code: string;
    nom: string;
    description: string | null;
    idempotent: boolean;
    actif: boolean;
  }>;
  apis: Array<{
    code: string;
    nom: string;
    environmentCode: string | null;
    fseApiCode?: string | null;
    operationCode: string;
    httpMethod: string;
    path: string;
    contentType: string | null;
    timeoutSeconds: number | null;
    requestMappingJson: string | null;
    responseMappingJson: string | null;
    retryEnabled: boolean;
    maxAttempts: number | null;
    retryDelayMs: number | null;
    backoffMultiplier: number | null;
    retryOn: string | null;
    actif: boolean;
  }>;
  flows: Array<{
    code: string;
    nom: string;
    description: string | null;
    actif: boolean;
    steps: Array<{
      code: string;
      stepType: StepType;
      ordre: number;
      apiOperationCode: string | null;
      nextStepSuccess: string | null;
      nextStepFailure: string | null;
      configurationJson: string | null;
      actif: boolean;
    }>;
  }>;
  statusMappings: Array<{
    providerStatus: string;
    gatewayStatus: PaymentStatus;
    messages?: Record<string, string> | null;
    /** @deprecated */
    message?: string | null;
  }>;
  otpConfig: {
    otpRequired: boolean;
    length: number | null;
    validitySeconds: number | null;
    maxAttempts: number | null;
    resendAllowed: boolean;
    resendDelaySeconds: number | null;
    generateOperation: string | null;
    validateOperation: string | null;
  } | null;
  webhooks: Array<{
    pathSuffix: string;
    authMode: string | null;
    allowedIps: string | null;
    actif: boolean;
  }>;
}

export interface ProviderConfigImportRequest {
  config: ProviderConfigExport;
  overwrite: boolean;
}

export type ProviderEnvironmentCode = 'SANDBOX' | 'DEVELOPPEMENT' | 'PRODUCTION';

export interface Environment {
  id: string;
  providerId: string;
  code: string;
  baseUrl: string;
  timeoutSeconds: number | null;
  actif: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EnvironmentRequest {
  code: ProviderEnvironmentCode | string;
  baseUrl: string;
  timeoutSeconds?: number | null;
  actif: boolean;
}

export interface Operation {
  id: string;
  providerId: string;
  code: string;
  nom: string;
  description: string | null;
  idempotent: boolean;
  actif: boolean;
  createdAt: string;
}

export interface OperationRequest {
  code: string;
  nom: string;
  description?: string | null;
  idempotent: boolean;
  actif: boolean;
}

export interface ProviderApi {
  id: string;
  providerId: string;
  fseApiId: string | null;
  fseApiCode: string | null;
  environmentId: string | null;
  operationId: string;
  code: string;
  nom: string;
  httpMethod: string;
  path: string;
  contentType: string | null;
  timeoutSeconds: number | null;
  requestMappingJson: string | null;
  responseMappingJson: string | null;
  retryEnabled: boolean;
  maxAttempts: number | null;
  retryDelayMs: number | null;
  backoffMultiplier: number | null;
  retryOn: string | null;
  actif: boolean;
  createdAt: string;
}

/** Api Omnicanal (connectivité) — parent des Méthodes. */
export interface FseApi {
  id: string;
  providerId: string;
  environmentId: string | null;
  environmentCode: string | null;
  code: string;
  nom: string;
  typeApi: string;
  baseUrl: string;
  host: string | null;
  port: string | null;
  useSsl: boolean;
  useProxy: boolean;
  actif: boolean;
  methodCount: number;
  authType: AuthType;
  clientId: string | null;
  tokenUrl: string | null;
  grantType: string | null;
  customHeaderName: string | null;
  hasClientSecret: boolean;
  hasApiKey: boolean;
  hasUsername: boolean;
  hasPassword: boolean;
  hasCustomHeaderValue: boolean;
  createdAt: string;
}

export interface FseApiRequest {
  code: string;
  nom: string;
  typeApi?: string | null;
  baseUrl: string;
  host?: string | null;
  port?: string | null;
  useSsl?: boolean;
  useProxy?: boolean;
  environmentId?: string | null;
  actif: boolean;
  authType?: AuthType | null;
  clientId?: string | null;
  clientSecret?: string | null;
  tokenUrl?: string | null;
  grantType?: string | null;
  username?: string | null;
  password?: string | null;
  apiKey?: string | null;
  customHeaderName?: string | null;
  customHeaderValue?: string | null;
}

export interface ProviderApiRequest {
  environmentId?: string | null;
  operationId: string;
  code: string;
  nom: string;
  httpMethod: string;
  path: string;
  contentType?: string | null;
  timeoutSeconds?: number | null;
  requestMappingJson?: string | null;
  responseMappingJson?: string | null;
  retryEnabled: boolean;
  maxAttempts?: number | null;
  retryDelayMs?: number | null;
  backoffMultiplier?: number | null;
  retryOn?: string | null;
  actif: boolean;
}

export interface AuthenticationConfig {
  id: string;
  providerId: string;
  environmentId: string;
  authType: AuthType;
  clientId: string | null;
  tokenUrl: string | null;
  customHeaderName: string | null;
  extraConfigJson: string | null;
  hasClientSecret: boolean;
  hasApiKey: boolean;
  hasUsername: boolean;
  hasPassword: boolean;
  hasCustomHeaderValue: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthenticationRequest {
  environmentId: string;
  authType: AuthType;
  clientId?: string | null;
  clientSecret?: string | null;
  apiKey?: string | null;
  tokenUrl?: string | null;
  username?: string | null;
  password?: string | null;
  customHeaderName?: string | null;
  customHeaderValue?: string | null;
  extraConfigJson?: string | null;
}

export interface FlowStep {
  id: string;
  flowId: string;
  code: string;
  stepType: StepType;
  ordre: number;
  apiOperationCode: string | null;
  nextStepSuccess: string | null;
  nextStepFailure: string | null;
  configurationJson: string | null;
  actif: boolean;
}

export interface FlowStepRequest {
  code: string;
  stepType: StepType;
  ordre: number;
  apiOperationCode?: string | null;
  nextStepSuccess?: string | null;
  nextStepFailure?: string | null;
  configurationJson?: string | null;
  actif: boolean;
}

export interface Flow {
  id: string;
  providerId: string;
  code: string;
  nom: string;
  serviceType: ServiceTypeCode;
  bpmProcessKey?: string | null;
  description: string | null;
  actif: boolean;
  createdAt: string;
  steps: FlowStep[];
}

export interface FlowRequest {
  code: string;
  nom: string;
  serviceType: ServiceTypeCode;
  bpmProcessKey?: string | null;
  description?: string | null;
  actif: boolean;
}

export interface StatusMapping {
  id: string;
  providerId: string;
  providerStatus: string;
  gatewayStatus: PaymentStatus;
  messages: Record<string, string> | null;
}

export interface StatusMappingRequest {
  providerStatus: string;
  gatewayStatus: PaymentStatus;
  messages?: Record<string, string> | null;
  /** @deprecated utiliser messages */
  message?: string | null;
}

export interface OtpConfig {
  id: string;
  providerId: string;
  otpRequired: boolean;
  length: number | null;
  validitySeconds: number | null;
  maxAttempts: number | null;
  resendAllowed: boolean;
  resendDelaySeconds: number | null;
  generateOperation: string | null;
  validateOperation: string | null;
}

export interface OtpConfigRequest {
  otpRequired: boolean;
  length?: number | null;
  validitySeconds?: number | null;
  maxAttempts?: number | null;
  resendAllowed: boolean;
  resendDelaySeconds?: number | null;
  generateOperation?: string | null;
  validateOperation?: string | null;
}

export interface WebhookConfig {
  id: string;
  providerId: string;
  pathSuffix: string;
  authMode: string | null;
  hasHmacSecret: boolean;
  hasApiKey: boolean;
  allowedIps: string | null;
  actif: boolean;
}

export interface WebhookConfigRequest {
  pathSuffix: string;
  authMode?: string | null;
  hmacSecret?: string | null;
  apiKey?: string | null;
  allowedIps?: string | null;
  actif: boolean;
}

export interface ConnectivityTestResponse {
  success: boolean;
  message: string;
  latencyMs: number | null;
}

export interface FormField {
  name: string;
  label: string;
  type: string;
  required: boolean | null;
  placeholder: string | null;
  pattern: string | null;
}

/** Native Form.io schema from nextAction.formSchema (Phase 2). */
export interface FormioSchemaDto {
  display?: string;
  components?: Record<string, unknown>[];
  [key: string]: unknown;
}

export interface NextAction {
  type: NextActionType;
  fields: FormField[] | null;
  formSchema?: FormioSchemaDto | null;
  url: string | null;
  expiresIn: number | null;
  otpLength: number | null;
  message: string | null;
  pollAfterSeconds: number | null;
}

export interface Payment {
  paymentId: string;
  provider: string;
  serviceType?: ServiceTypeCode;
  catalogServiceCode?: string | null;
  catalogServiceVersionId?: string | null;
  catalogServiceVersion?: string | null;
  amount: number;
  currency: string;
  status: PaymentStatus;
  currentStep: string | null;
  providerReference: string | null;
  customerReference: string | null;
  phoneNumber: string | null;
  nextAction: NextAction | null;
  errorCode: string | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
  /** Libellés serveur (Accept-Language) — listes supervision. */
  title?: string | null;
  subtitle?: string | null;
  productLabel?: string | null;
  statusLabel?: string | null;
  /** Point de retrait / dépôt (agent, agence). */
  agentDisplay?: string | null;
  withdrawalChannel?: string | null;
}

export interface CreatePaymentRequest {
  /** Wallet / canal d'exécution. Optionnel si le catalogue définit defaultProvider / defaultPayProvider. */
  provider?: string;
  serviceType?: ServiceTypeCode;
  catalogServiceCode?: string;
  catalogServiceVersionId?: string;
  amount: number;
  currency?: string;
  customerReference?: string;
  merchantReference?: string;
  phoneNumber?: string;
  data?: Record<string, unknown>;
}

export interface PaymentEvent {
  timestamp: string;
  eventType: string;
  message: string;
}

export interface DailyStats {
  date: string;
  total: number;
  success: number;
  pending: number;
  failed: number;
  successRate: number;
  byStatus: Record<string, number>;
}

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName: string | null;
  enabled: boolean;
  locked: boolean;
  roles: string[];
  createdAt: string;
  lastLoginAt: string | null;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string | null;
  roles: string[];
}

export interface AuditLog {
  id: string;
  actorUsername: string | null;
  action: string;
  resourceType: string | null;
  resourceId: string | null;
  details: string | null;
  ipAddress: string | null;
  traceId: string | null;
  createdAt: string;
}

export type ProprieteApi = 'FSESOURCE' | 'FSEDESTINATAIRE' | 'FSEPARTENAIRE';
export type TypeEnvoi = 'REQUEST';
export type ImputeAgence = 'AGENCESOURCE' | 'AGENCEDEST' | 'AGENCESIEGESOURCE' | 'AGENCESIEGEDEST';
export type ParametreCategorie = 'USER' | 'COMPTE' | 'AGENCE' | 'SYSTEM';

export interface SchemaEcritureRequest {
  nomSchema: string;
  serviceType: ServiceTypeCode;
  patternCode: string;
  workflowName?: string | null;
  workflowVersion?: number;
  engine?: string;
  errorPolicyCode?: string | null;
  actif: boolean;
}

export interface LigneSchemaEcritureRequest {
  schemaEcritureId: string;
  ordre: number;
  stepId?: string | null;
  typeMethode: string;
  apiAUtiliser: ProprieteApi;
  agenceImputer?: ImputeAgence | null;
  executionMode?: string | null;
  timeoutPolicy?: string | null;
  timeoutDuration?: string | null;
  callbackPolicy?: string | null;
  statusMethod?: string | null;
  retryPolicy?: string | null;
  retryMaxAttempts?: number | null;
  retryInitialDelayMs?: number | null;
  retryMultiplier?: number | null;
  retryMaxDelayMs?: number | null;
  retryableCodes?: string | null;
  idempotencyPolicy?: string | null;
  compensationPolicy?: string | null;
  compensationMode?: string | null;
  compensationStepCode?: string | null;
  compensationReverseDesc?: boolean | null;
  compensationMaxAttempts?: number | null;
  compensationManualReview?: boolean | null;
  dependsOnStepCode?: string | null;
  dependencyCondition?: string | null;
  dependencyRequired?: boolean | null;
  errorPolicies?: string | null;
  errorPolicyCode?: string | null;
  defaultErrorAction?: string | null;
  checkStatusMethodCode?: string | null;
  maxRetries?: number | null;
  blocking?: boolean | null;
  criticality?: string | null;
}

export interface ParametreSchemaRequest {
  ligneSchemaEcritureId: string;
  nomParametre?: string | null;
  codeParametre: string;
  codeParametreMethode?: string | null;
  nomExterne?: string | null;
  typeParametre?: string | null;
  categorie?: ParametreCategorie;
  valeurDefaut?: string | null;
}

export interface SchemaEcriture {
  id: string;
  nomSchema: string;
  serviceType: ServiceTypeCode;
  patternCode: string;
  workflowName: string | null;
  workflowVersion: number;
  engine: string;
  errorPolicyCode: string | null;
  actif: boolean;
  createdAt: string;
  lignes: LigneSchemaEcriture[];
}

export interface LigneSchemaEcriture {
  id: string;
  schemaEcritureId: string;
  ordre: number;
  stepId: string | null;
  typeMethode: string;
  apiAUtiliser: ProprieteApi;
  agenceImputer: ImputeAgence | null;
  executionMode: string | null;
  timeoutPolicy: string | null;
  timeoutDuration: string | null;
  callbackPolicy: string | null;
  statusMethod: string | null;
  retryPolicy: string | null;
  retryMaxAttempts: number | null;
  retryInitialDelayMs: number | null;
  retryMultiplier: number | null;
  retryMaxDelayMs: number | null;
  retryableCodes: string | null;
  idempotencyPolicy: string | null;
  compensationPolicy: string | null;
  compensationMode: string | null;
  compensationStepCode: string | null;
  compensationReverseDesc: boolean | null;
  compensationMaxAttempts: number | null;
  compensationManualReview: boolean | null;
  dependsOnStepCode: string | null;
  dependencyCondition: string | null;
  dependencyRequired: boolean | null;
  errorPolicies: string | null;
  errorPolicyCode: string | null;
  defaultErrorAction: string | null;
  checkStatusMethodCode: string | null;
  maxRetries: number | null;
  blocking: boolean | null;
  criticality: string | null;
  createdAt?: string;
  parametres: ParametreSchema[];
}

export interface ParametreSchema {
  id: string;
  ligneSchemaEcritureId: string;
  nomParametre: string | null;
  codeParametre: string;
  codeParametreMethode: string | null;
  nomExterne: string | null;
  typeParametre: string | null;
  categorie: ParametreCategorie;
  valeurDefaut: string | null;
  createdAt?: string;
}

export interface OrchestrationCatalog {
  patternCodes: string[];
  typeMethodes: string[];
  executionModes: string[];
  timeoutPolicies: string[];
  callbackPolicies: string[];
  retryPolicies: string[];
  idempotencyPolicies: string[];
  compensationPolicies: string[];
  compensationModes: string[];
  criticalities: string[];
  errorActions: string[];
  dependencyConditions: string[];
  paramTypes: string[];
}

/** Corridor inter-FSE (relation_fse + comptes LORO / MIROIR). */
export interface RelationFseCorridor {
  id: string;
  fseSource: string;
  fseDestination: string;
  serviceType: ServiceTypeCode;
  createdAt: string;
  comptes: CompteFseItem[];
}

export interface CompteFseItem {
  id: string;
  numeroCompte: string;
  typeCompteCode: string;
  typeCompteLibelle: string;
  fseTeneur: string;
  fseTitulaire?: string | null;
  codeBanque?: string | null;
  codeAgence?: string | null;
  cle?: string | null;
  devise?: string | null;
  agenceCode?: string | null;
}

/** Compte catalogue — schéma aligné Omnicanal. */
export interface CompteFse {
  id: string;
  omniId: number | null;
  numeroCompte: string;
  codeBanque: string;
  codeAgence: string;
  cle: string | null;
  extype: string;
  typeCompteCode: string;
  typeCompteLibelle: string;
  devise: string | null;
  fseTeneur: string;
  fseTitulaire: string;
  serviceType: ServiceTypeCode | null;
  libelleCompte: string;
  codeUoTitulaire: string | null;
  mirroirId: string | null;
  agenceCode: string;
  relationId: string | null;
  fseSource: string | null;
  fseDestination: string | null;
  relationServiceType: ServiceTypeCode | null;
  createdAt: string;
}

export interface CompteFseRequest {
  numeroCompte: string;
  typeCompteCode: string;
  fseTeneur: string;
  fseTitulaire: string;
  codeBanque: string;
  codeAgence?: string | null;
  cle?: string | null;
  devise?: string | null;
  libelleCompte?: string | null;
  codeUoTitulaire?: string | null;
  agenceCode?: string | null;
  extype?: string | null;
  serviceType?: ServiceTypeCode | null;
  relationId?: string | null;
  mirroirId?: string | null;
}

export interface TypeCompte {
  codeParametre: string;
  libelle: string;
}

export interface RelationFseCorridorRequest {
  fseSource: string;
  fseDestination: string;
  serviceType: ServiceTypeCode;
  numeroCompteLoroSource: string;
  numeroCompteMiroirDest: string;
  alsoCreateReverse?: boolean;
}

export interface InterfaceUtilisateur {
  id: string;
  providerId: string;
  providerCode: string;
  codeFse: string;
  serviceType: ServiceTypeCode;
  typeMethode: string;
  typeEnvoi: TypeEnvoi;
  providerOperationId: string | null;
  providerOperationCode: string | null;
  providerApiId: string | null;
  providerApiCode: string | null;
  codeInterface: string | null;
  nomInterface: string | null;
  actif: boolean;
  createdAt: string;
  parametres?: ParametreUtilisateur[];
}

export interface InterfaceUtilisateurRequest {
  codeFse: string;
  serviceType: ServiceTypeCode;
  typeMethode: string;
  typeEnvoi?: TypeEnvoi | null;
  providerOperationId?: string | null;
  providerApiId?: string | null;
  codeInterface?: string | null;
  nomInterface?: string | null;
  actif: boolean;
}

export interface InterfaceParamTemplatePreview {
  codeParametre: string;
  codeParametreMethode: string | null;
  attribut: string | null;
  typeParametre: string;
  ordre: number;
}

export interface InterfaceTemplatePreview {
  typeMethode: string;
  suggestedServiceType: ServiceTypeCode;
  suggestedOperationId: string | null;
  suggestedOperationCode: string | null;
  suggestedApiId: string | null;
  suggestedApiCode: string | null;
  defaultParametres: InterfaceParamTemplatePreview[];
}

export interface ParametreUtilisateur {
  id: string;
  interfaceUtilisateurId: string;
  nomParametre: string | null;
  codeParametre: string;
  codeParametreMethode: string | null;
  typeParametre: string | null;
  valeur: string | null;
  parametreApi: ProprieteApi | null;
  attribut: string | null;
  sourceParametre?: string | null;
  extractScript?: string | null;
  /** Clé i18n (ex. bill.amount) */
  labelKey?: string | null;
  /** Libellés inline JSON {"fr":"...","en":"..."} */
  labelsJson?: string | null;
  /** Exposé UI (défaut true) */
  affiche?: boolean;
  ordre: number;
  createdAt?: string;
}

export interface ParametreUtilisateurRequest {
  nomParametre?: string | null;
  codeParametre: string;
  codeParametreMethode?: string | null;
  typeParametre?: string | null;
  valeur?: string | null;
  parametreApi?: ProprieteApi | null;
  attribut?: string | null;
  sourceParametre?: string | null;
  extractScript?: string | null;
  labelKey?: string | null;
  labelsJson?: string | null;
  affiche?: boolean | null;
  ordre?: number | null;
}

export interface SchemaMatchResult {
  schemaId: string;
  nomSchema: string;
  patternCode: string;
  workflowName: string | null;
  workflowVersion: number;
  serviceType: ServiceTypeCode;
  lignes: LigneSchemaEcriture[];
}

export interface SchemaExecuteRequest {
  serviceType: ServiceTypeCode;
  sourceCodeFse: string;
  destCodeFse: string;
  schemaEcritureId?: string;
  paymentId?: string;
  siege?: number;
  userParams?: Record<string, string>;
}

export interface SchemaLegResult {
  ligneSchemaEcritureId: string | null;
  stepId: string;
  ordre: number | null;
  codeFse: string;
  typeMethode: string;
  apiAUtiliser: ProprieteApi;
  providerCode: string;
  operationCode: string;
  success: boolean;
  providerStatus: string | null;
  providerMessage: string | null;
  providerReference: string | null;
  gatewayStatus: string;
}

export interface SchemaExecuteResponse {
  schemaId: string;
  nomSchema: string;
  patternCode: string;
  workflowName: string | null;
  serviceType: ServiceTypeCode;
  success: boolean;
  message: string;
  legs: SchemaLegResult[];
}

export interface TransactionSearchParams {
  paymentId?: string;
  provider?: string;
  status?: string;
  customerReference?: string;
  providerReference?: string;
  phoneNumber?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
  sort?: string;
}
