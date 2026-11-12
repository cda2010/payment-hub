package com.pti.emeraude.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pti.emeraude.gateway.domain.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.pti.emeraude.gateway.enumeration.TypeService;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import org.apache.http.HttpEntityEnclosingRequest;
import static com.pti.emeraude.gateway.domain.HttpClientConfigConstants.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author cda
 */
public class PtiSetup {

    private final Logger log = LoggerFactory.getLogger(PtiSetup.class);
    private String restServerUri;
    private String secureServerUri;
    private String grantUri;
    private String ressource;
    private String accessToken;
    private String login;
    private String mode;
    private String password;
    private String plainClientCredentials;
    private String clientID;
    private String url;

    private AuthTokenInfo tokenInfo;
    private final ConcurrentHashMap<String, AuthTokenInfo> cache = new ConcurrentHashMap<>();

    final String LIVE_REGLE_VERIF_FOURN_BASE_URL = "/api/mobile/omnnicanal/getreglefacturier?montant={montant}&reference={reference}&idFacturier={idFacturier}";
    final String LIVE_PAIEMENT_FACTURE = "/api/external/operations/paiement-facture";
    final String LIVE_LINK_WALLET = "/api/external/operations/souscription-wallet";
    final String LIVE_LIST_WALLET = "/api/external/operations/list-wallet-card";
    final String LIVE_SOLDE_CARTE = "/api/external/card/v1/getSolde";
    final String LIVE_CLIENT_LIST_WALLET = "/api/external/operations/list-wallet";
    final String LIVE_LIST_ORGANISATION_FILS = "/api/mobile/uo/listUoForPickup?codeUo={codeUo}&type={type}";
    final String LIVE_PERSONNE_MORALE_BASE_URL = "/api/mobile/personne/find-personne?telephone={telephone}&code={code}&email={email}&qualif={qualif}";
    final String LIVE_COMPTE_VERIF_BASE_URL = "/api/mobile/compte/verifIntegrite?numeroCompte={numeroCompte}&acteur={acteur}&identifiant={identifiant}&codeIndicatif={codeIndicatif}";
    final String LIVE_RECLA_CREATE_BASE_URL = "/api/mobile/type-reclamation/create";
    final String LIVE_CLIENT_DESACTIVE_BASE_URL = "/api/mobile/client/deactive";
    final String LIVE_AGENT_DESACTIVE_BASE_URL = "/api/mobile/agent/deactive";
    final String LIVE_LIST_CLASS_BASE_URL = "/api/mobile/classification-facturiers/of-Uo?code={code}";
    final String LIVE_MAJ_ALIAS_PV_BASE_URL = "/api/mobile/pointVente/majAlias?codePv={codePv}&alias={alias}";

    final String LIVE_LIST_FACTURIER_TEL_BASE_URL = "/api/mobile/facturiers/telephonie?codeFse={codeFse}";

    final String LIVE_LIST_FACTURIER_BASE_URL = "/api/mobile/facturiers/of-classification?id={id}";

    final String LIVE_LIST_FACTURIER_CANAL_BASE_URL = "/api/mobile/facturiers/of-classification?id={id}&canal={canal}";

    final String LIVE_LIST_PRODUIT_BASE_URL = "/api/mobile/produits/of-facturier?id={id}";

    final String LIVE_LIST_ECRAN_BASE_URL = "/api/mobile/ecrans/of-produit?id={id}";

    final String LIVE_RECLA_LIST_BASE_URL = "/api/mobile/type-reclamation/list";

    final String LIVE_OP_ENVOI_BASE_URL = "/api/mobile/operations/do_op_envoi";

    final String LIVE_OP_TRANSFERT_COMPTE_BASE_URL = "/api/mobile/operations/do_op_transfert_cash_vers_compte";

    final String LIVE_OP_RETRAIT_COMPTE_BASE_URL = "/api/mobile/operations/do_op_retrait_compte";
    final String LIVE_LAST_OPERATION_CLIENT_BASE_URL = "/api/operation/LastThreeOperation-optimise?code={code}&telephone={telephone}";

    final String LIVE_OPERATION_CLIENT_BASE_URL = "/api/mobile/operations/listeOperationAttente?valeurCompte={valeurCompte}&typeCompte={typeCompte}&codeAgence={codeAgence}&codeFseTeneur={codeFseTeneur}&typeServ={typeServ}";

    final String LIVE_LISTPV_BASE_URL = "/api/mobile/pointvente/listePointVente?code={code}";
    final String LIVE_VERIFPV_BASE_URL = "/api/mobile/pointvente/verifPv?codePv={codePv}&codeActeur={codeActeur}";
    final String LIVE_AFFECTPV_BASE_URL = "/api/mobile/pointvente/affectation?codePv={codePv}&codeActeur={codeActeur}&login={login}";
    final String LIVE_BLOCAGE_TRANS_DETAIL_URL = "/api/mobile/operations/trans/detail?codeoperation={codeoperation}";

    final String LIVE_CHANGE_PASSWORD_CLIENT_BASE_URL = "/api/mobile/client/changepassword?login={login}&password={password}";

    final String LIVE_BLOCAGE_TRANS_BASE_URL = "/api/mobile/operations/trans/blocage?codeoperation={codeoperation}";
    final String LIVE_VERIF_RETRAIT_BASE_URL = "/api/mobile/operations/retraitData";
    final String LIVE_OP_BASE_URL = "/api/mobile/operations/do_op";

    final String LIVE_SWEEP_BASE_URL = "/api/mobile/operations/do_sweep";

    final String LIVE_OP_FACTURE_URL = "/api/mobile/operations/do_facturepaiement";
    final String LIVE_OP_CREDIT_URL = "/api/mobile/operations/do_facturecredit";
    final String LIVE_OP_FACTURE_CASH_URL = "/api/mobile/operations/do_facturepaiementcash";
    final String LIVE_LIST_AUTORISATION_ORGANISATION = "/api/mobile/autorisation/listAutorisation?codeUo={codeUo}";

    final String LIVE_LIST_AUTORISATION_CLIENT = "/api/mobile/client/autorisations/?code={code}&telephone={telephone}";

    final String LIVE_USER_CREATE_BASE_URL = "/api/mobile/users/create";
    final String LIVE_OP_RETRAIT_BASE_URL = "/api/mobile/operations/do_op_retrait";
    final String LIVE_OP_RETRAIT_CASH_URL = "/api/mobile/operations/do_op_retrait_cash";

    final String LIVE_OP_APPROVISIONNEMENT = "/api/mobile/operations/do_approvisionnement";

    final String LISTE_OPERATION_ALL_TRANSACTION = "/api/operations/mobile/all?page={page}&pageSize={pageSize}&typeServiceId={typeServiceId}&canalId={canalId}&uoAcquereurId={uoAcquereurId}&statutOperation={statutOperation}&dateOperation={dateOperation}&dateFin={dateFin}&codeOperation={codeOperation}&utilisateur={utilisateur}";

    final String LIVE_PAYS_URL = "/api/mobile/pays/infos";
    final String LIVE_MOTIF_URL = "/api/mobile/operations/motifs";
    final String LIVE_FAQ_URL = "/api/mobile/operations/faq";

    final String LIVE_GET_UO_BYCODE_URL = "/api/mobile/organisation/code?codeUo={codeUo}";

    final String LIVE_GET_UO_BYCODEPV_URL = "/api/mobile//organisation/getFromPv?codePv={codePv}";

    final String LIVE_TYPEPIECE_URL = "/api/mobile/type-piece/mob-typepiece";
    final String LIVE_TYPEDOC_URL = "/api/mobile/type-documents/mob-typedocument";
    final String LIVE_DATA_PARAMETERS_PAYS = "/api/mobile/donnee/dataParameters?code={code}";
    final String LIVE_DATA_PARAMETERS = "/api/mobile/donnee/dataParameters";
    final String LIVE_INCRIRE_TERMINAL = "/api/mobile/operations/do_inscriptionTerminal";

    final String LIVE_MONTANT_FACTURE = "/api/mobile/omnnicanal/get_montant";
    final String LIVE_LIST_FACTURE_FOURNISSEUR = "/api/mobile/omnnicanal/facture-founisseur/list";

    final String LISTE_PERSONNE_RECHERCHE_TELEPHONE = "/api/personnes/recherche-telephone?page={page}&pageSize={pageSize}&telephone={telephone}&reference={reference}&typeId={typeId}&codeTelephone={codeTelephone}";

    final String LIVE_LIST_POINT_VENTE_LOCALISATION = "/api/mobile/geo-localisation/list?codeFse={codeFse}&minLat={minLat}&maxLat={maxLat}&minLon={minLon}&maxLon={maxLon}";

    final String LIVE_ANNULATION_OPERATION = "/api/mobile/operations/annulation?codeOperation={codeOperation}";
    final String LISTE_NOTIFICATION_TRANSACTION = "/api/mobile/notifications/listeNotifcation?numero={numero}&codeIndicatif={codeIndicatif}";
    final String LIVE_TRANS_PARAMETERS_NEW = "/api/mobile/donnee/dataTransaction?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}";

    final String LIVE_TRANS_PARAMETERS_ACTEUR_NEW = "/api/mobile/donnee/dataTransaction?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}&codeacteur={codeacteur}";

    final String LIVE_TRANS_PARAMETERS = "/api/mobile/donnee/dataTransaction?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}";

    final String LIVE_TRANS_PARAMETERS_ACTEUR = "/api/mobile/donnee/dataTransaction?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeacteur={codeacteur}";

    final String LIVE_FSE_OF_PAYS = "/api/mobile/pays/list_fse?code={code}";
    final String COMPTE_INFOSCLIENT = "/api/mobile/omnnicanal/getInfosClient?agence={agence}&numeroCompte={numeroCompte}&codeFse={codeFse}";
    final String LIVE_IF_EXIT_CLIENT_BASE_URL = "/api/mobile/client/infosexist?telephone={telephone}&code={code}";
    final String COMPTE_SOLDE = "/api/mobile/omnnicanal/get_solde?agence={agence}&numeroCompte={numeroCompte}&codeFse={codeFse}&acteur={acteur}&codeIndicatif={codeIndicatif}&identifiant={identifiant}";
    final String GET_INFO = "/api/mobile/omnnicanal/get_infos";

    final String LIVE_GET_STOPPAGE = "/api/omnnicanal/getStoppage?agence={agence}&numeroCompte={numeroCompte}&codeFse={codeFse}&montant={montant}&isInit={isInit}&eventId={eventId}&sens={sens}";

    final String GET_INFO_RECU = "/api/mobile/recu/get_infos_recu";

    final String GET_PRINT = "/api/mobile/omnnicanal/get_print";

    final String LIVE_VERIF_CODE_OTP = "/api/mobile/omnnicanal/otp/verifCodeOtp?codeFse={codeFse}&otp={otp}&identifiant={identifiant}";

    final String GET_DATA = "/api/mobile/omnnicanal/get_data";
    final String COMPTE_HISTORIQUE_TRANSACTION = "/api/mobile/omnnicanal/consultation_historique?agence={agence}&numeroCompte={numeroCompte}&codeFse={codeFse}&debutPeriode={debutPeriode}&finPeriode={finPeriode}&acteur={acteur}&codeIndicatif={codeIndicatif}&identifiant={identifiant}";
    final String SEND_SMS = "/api/mobile/omnnicanal/sms?telephone={telephone}&message={message}";
    final String SEND_MAIL = "/api/mobile/omnnicanal/mail?mail={mail}&message={message}";
    final String LIVE_SOUSCRIPTION_BASE_URL = "/api/mobile/operations/do_souscription";
    final String LIVE_CALCUL_FRAIS = "/api/mobile/operations/calcul_frais?canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}";
    final String LIVE_COMPTES_CLIENT_BASE_URL = "/api/mobile/comptes/infos?code={code}&telephone={telephone}";
    final String LIVE_INFOS_CLIENT_BASE_URL = "/api/mobile/personnes/all_infos?code={code}&telephone={telephone}";
    final String LIVE_INFOS_ACTEUR_BASE_URL = "/api/mobile/infos/acteur/?code={code}&telephone={telephone}&codeActeur={codeActeur}&acteur={acteur}";

    final String LIVE_INFOS_ORGA_BASE_URL = "/api/mobile/infos/orga/?codeActeur={codeActeur}";

    final String LIVE_LIST_PRODUIT_MRC_BASE_URL = "/api/mobile/produits/of-facturier-uo?codeUo={codeUo}";
    final String LIVE_LIST_IS_AUTH_FORTE_BASE_URL = "/api/mobile/user/authentificationforte";

    final String LIVE_LIST_TAUX_CHANGE_BASE_URL = "/api/mobile/tauxchange/tauxchangedujour?devArrivee={devArrivee}&devDepart={devDepart}&sens={sens}";

    final String LIVE_LIST_FACTURE_MRC_BASE_URL = "/api/mobile/factures/of-facturier-uo?id={id}";

    final String LIVE_COMPTES_AGENT_BASE_URL = "/api/mobile/comptes/agent?codeUo={codeUo}";
    final String LIVE_FILE_TRANSFERT_BASE_URL = "/api/upload/chargement/fichier";

    final String LIVE_FILE_LISTE_OPERATION_BASE_URL = "/api/operations/mobile/all?typeServiceId={typeServiceId}&canalId={canalId}&uoAcquereurId={uoAcquereurId}&montantOp={montantOp}&statutOperation={statutOperation}&dateOperation={dateOperation}&dateFin={dateFin}&codeOperation={codeOperation}&organisationId={organisationId}";

    final String LIVE_FILE_LISTE_CHARGEMENT_BASE_URL = "/api/chargement-transferts/liste-charge?telephone={telephone}&code={code}&statut={statut}";
    final String LIVE_VALIDE_CHARGEMENT_BASE_URL = "/api/chargement-transferts/valider/mobile?id={id}&user={user}&canal={canal}";

    final String LIVE_VERIFE_CLIENT_BASE_URL = "/api/mobile/client/verification?codePays={codePays}&login={login}&password={password}";
    final String LIVE_AGENT_GAB_BASE_URL = "/api/mobile/operations/gab/agent?codePays={codePays}";
    final String LIVE_DELETE_ENTRY_CLIENT_BASE_URL = "/api/mobile/client/deleteentry?id={id}";

    final String LIVE_FACTURIER_OFUO_BASE_URL = "/api/mobile/facturiers/of-marchand-uo?codeUo={codeUo}";
    final String LIST_RESEAU_ENTREPRISE = "/api/mobile/entreprise/reseaux?telephone={telephone}&codePays={codePays}";

    final String EXTERNAL_VERIFICATION = "/verification/verification-hash-param?params={params}&hashParam={hashParam}&client_id={client_id}";
    final String EXTERNAL_GET_SOLDE = "/api/external/solde?codePays={codePays}&telephone={telephone}";
    final String EXTERNAL_GET_INFOS_CLIENT = "/api/external/infos-client?codePays={codePays}&telephone={telephone}";
    final String EXTERNAL_GET_HISTORIQUE = "/api/external/historique?codePays={codePays}&telephone={telephone}&debutPeriode={debutPeriode}&finPeriode={finPeriode}";
    final String EXTERNAL_SEND_CODE_OTP = "/api/external/send-code-otp?codePays={codePays}&telephone={telephone}";
    final String EXTERNAL_CALCUL_FRAIS = "/api/external/operations/calcul-frais?typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}";
    final String EXTERNAL_PAIEMENT_BIEN = "/api/external/operations/paiement-bien?codePays={codePays}&telephone={telephone}&codePv={codePv}&montant={montant}&codeOTP={codeOTP}";

    final String EXTERNAL_RETRAIT_COMPTE = "/api/external/operations/retrait-compte?codePays={codePays}&telephone={telephone}&codePv={codePv}&montant={montant}&codeOTP={codeOTP}";
    final String EXTERNAL_ACCOUNT_ACCOUNT = "/api/external/operations/account-account?codePaysEmetteur={codePaysEmetteur}&telephoneEmetteur={telephoneEmetteur}&codePaysRecepteur={codePaysRecepteur}&telephoneRecepteur={telephoneRecepteur}&codePv={codePv}&montant={montant}&codeOTP={codeOTP}";
    final String EXTERNAL_PAIEMENT_INTERNET = "/api/external/operations/paiement-internet?codePays={codePays}&telephone={telephone}&codePv={codePv}&codeRetrait={codeRetrait}&montant={montant}&isMontant={isMontant}";

    final String EXTERNAL_PAIEMENT_DEPOT = "/api/external/operations/deposit?codePaysRecepteur={codePaysRecepteur}&telephoneRecepteur={telephoneRecepteur}&codePv={codePv}&montant={montant}&clientId={clientId}";

    final String COMPTE_MINI_RELEVE = "/api/mobile/omnnicanal/mini-releve?agence={agence}&numeroCompte={numeroCompte}&codeFse={codeFse}&acteur={acteur}&codeIndicatif={codeIndicatif}&identifiant={identifiant}";

    private static final String LIVE_LINK_URL = "api/v1/operations/link";
    private static final String LIVE_DELINK_URL = "api/v1/operations/delink";

    private String COMPTE_INFOSCLIENT_ALIAS = "/api/mobile/omnnicanal/getInfosClientAlias?alias={alias}&codeFse={codeFse}";
    private static final String RESEND_MESSAGE_OPERATION = "/api/mobile/operations/reSendMessage?codeOperation={codeOperation}";

    private static final String LIVE_GET_LIST_PUBLICITE = "/api/mobile/publicite/listePublicite?codeIndicatif={codeIndicatif}";

    private static final String LIVE_REQUEST_CARDLESS_URL = "api/sendCardless?telephone={telephone}&reference={reference}&montant={montant}&entityCode={entityCode}";
    private static final String LIVE_VIREMENT_EXT_WALLET_URL = "api/v1/operations/transfert/?numeroCompte={numeroCompte}&msisdn={msisdn}&codePays={codePays}&montantOp={montantOp}&codeOperateur={codeOperateur}&api={api}&codePv={codePv}&referenceExterne={referenceExterne}&bankCible={bankCible}&alias={alias}&canal={canal}";
    private static final String EXTERNAL_OPERATION_STATUS = "/api/external/operations/status?codeOperation={codeOperation}";

    private static final String EXTERNAL_FACTURIERS_BY_CODE_UO = "/api/external/facturiers/data/of-uo?codeUo={codeUo}&secteur={secteur}";

    private static final String EXTERNAL_CODE_OPERATION_STATUS = "/api/external/operations/status/externe?codeOperation={codeOperation}";

    private static final String EXTERNAL_OPERATION_ANNULATION = "/api/v1/operations/annulation?codeOperation={codeOperation}&codePv={codePv}";
    final String EXTERNAL_GET_INFOS_FACTURE = "/api/external/facturier/get-info";
    final String EXTERNAL_GET_LIST_FACTURES = "/api/external/facturier/list-facture";

    private static final String EXTERNAL_GET_OPERATION_FEES = "/api/external/operations/getFees";

    private static final String LIVE_REQUEST_CARDLESS_NEW_URL = "api/getVoucherCardLess?telephone={telephone}&reference={reference}&montant={montant}&entityCode={entityCode}&codeFse={codeFse}";

    private static final String LIVE_REQUEST_STATUS_CARDLESS_NEW_URL = "api/getCardlessStatus?reference={reference}&entityCode={entityCode}&codeFse={codeFse}";

    private static final String EXTERNAL_GET_LIST_PAIEMENT_FACTURE = "/api/external/facturier/get-liste-paiement-facture?codePays={codePays}&telephone={telephone}&codeFaturier={codeFaturier}&taille={taille}&page={page}";

    final String SAVE_OPERATION_URL = "api/operation/update_op?codeOperation={codeOperation}&codeAgence={codeAgence}&codeBanque={codeBanque}&numeroCompte={numeroCompte}&rib={rib}&ref={ref}";
    final String LISTE_OPERATION_PARTENAIRE_URL = "api/operations/list-operation-partenaire?codePv={codePv}&dateDebut={dateDebut}&dateFin={dateFin}";

    final String VIREMENT_RTGS_URL = "/api/mobile/rtgs/virement?codeFse={codeFse}&compteDebiteur={compteDebiteur}&codeAgenceDebit={codeAgenceDebit}&referenceExterne={referenceExterne}&compteCredit={compteCredit}&montantDebit={montantDebit}&codeAgenceCredit={codeAgenceCredit}&codeBanqueCredit={codeBanqueCredit}&codeBanqueDebit={codeBanqueDebit}&nomBeneficiaire={nomBeneficiaire}&nomDonneurOrdre={nomDonneurOrdre}&description={description}&canal={canal}&externe={externe}&cle={cle}";

    final String EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL = "/api/external-gateway/omnnicanal/v1";
    final String EXTRERNAL_CARD_URL = "/api/external/card/v1";
    final String EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS = "/api/external-gateway/omnnicanal/v1/operations/dataTransaction?codeFse={codeFse}&code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}";
    final String EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS_ACTEUR = "/api/external-gateway/omnnicanal/v1/operations/dataTransaction?codeFse={codeFse}&code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}&codeacteur={codeacteur}";

    final String TOKEN_URL = "/oauth/token";
    final String STATUS_CODE = "1";
    final String MSG = "Opération effectué avec succès.";
    final String LIVE_VERIFICATION_CHARGEMENT_BASE_URL = "/api/chargement-transferts/verification/mobile?id={id}&user={user}&canal={canal}";

    private static final String EXTERNAL_SEND_OTP = "/api/external/send-code";
    private final String EXTERNAL_CONTEXTE_GET_FACTURIER = "/api/mobile/facturiers/facturier?codeUo={codeUo}&secteur={secteur}";
    final String LIVE_GEN_CODE_OTP = "/api/mobile/omnnicanal/genererOtp?codeFse={codeFse}&numeroCompte={numeroCompte}&agence={agence}&teneur={teneur}&codeProduit={codeProduit}";

    final String AJOUT_RETRAIT_NUMERO_CARDLESS = "/api/mobile/operations/action_numero_cardless?codePv={codePv}&codeActeur={codeActeur}&login={login}&action={action}";

    final String LIVE_OP_RETRAIT_CARDLESS_AGENT = "/api/mobile/operations/do_op_cardless_agent";
    //chatbot
    final String CHATBOT_COMO_BASE_URL = "/api/chatbot/chatComo";

    private static final String LIVE_PAIEMENT_MARCHAND_EXT_WALLET_URL = "api/v1/operations/paiementMarchand/?numeroCompte={numeroCompte}&codePvSrc={codePvSrc}&codePvDest={codePvDest}&montantOp={montantOp}&codeOperateur={codeOperateur}&codePv={codePv}&referenceExterne={referenceExterne}&bankCible={bankCible}&donneurOrdre={donneurOrdre}&beneficiaire={beneficiaire}";

    final String INTEROP_CONTEXTE_LIVE_TRANS_PARAMETERS = "/api/interop-gateway/omnnicanal/v1/donnee/dataTransactionExterne?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}";
    final String INTEROP_CONTEXTE_LIVE_TRANS_PARAMETERS_ACTEUR = "/api/interop-gateway/omnnicanal/v1/donnee/dataTransactionExterne?code={code}&telephone={telephone}&canal={canal}&typeService={typeService}&codePv={codePv}&codeUoDest={codeUoDest}&codeUoSrc={codeUoSrc}&montant={montant}&codeAgence={codeAgence}&typeCompte={typeCompte}&valeurCompte={valeurCompte}&teneur={teneur}&codeacteur={codeacteur}";
    final String INTEROP_CONTEXT_LIVE_OP_BASE_URL = "/api/interop-gateway/omnnicanal/v1";

    public PtiSetup() {
    }

    public PtiSetup(String restServerUri, String secureServerUri, String grantUri, String ressource, String accessToken, String login, String mode, String password, String plainClientCredentials, String clientID, String url) {
        this.restServerUri = restServerUri;
        this.secureServerUri = secureServerUri;
        this.grantUri = grantUri;
        this.ressource = ressource;
        this.accessToken = accessToken;
        this.login = login;
        this.mode = mode;
        this.password = password;
        this.plainClientCredentials = plainClientCredentials;
        this.clientID = clientID;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRestServerUri() {
        return restServerUri;
    }

    public void setRestServerUri(String restServerUri) {
        this.restServerUri = restServerUri;
    }

    public String getSecureServerUri() {
        return secureServerUri;
    }

    public void setSecureServerUri(String secureServerUri) {
        this.secureServerUri = secureServerUri;
    }

    public String getGrantUri() {
        return grantUri;
    }

    public void setGrantUri(String grantUri) {
        this.grantUri = grantUri;
    }

    public String getRessource() {
        return ressource;
    }

    public void setRessource(String ressource) {
        this.ressource = ressource;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlainClientCredentials() {
        return plainClientCredentials;
    }

    public void setPlainClientCredentials(String plainClientCredentials) {
        this.plainClientCredentials = plainClientCredentials;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAccountToCashUrl() {

        return this.mode + "_ACCOUNT_TO_CASH_BASE_URL";

    }

    public String getAccountToAccountUrl() {
        return this.mode + "_ACCOUNT_TO_ACCOUNT_BASE_URL";
    }

    private static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public AuthTokenInfo tokenCache() {
        final String key = "TOKEN";
        AuthTokenInfo resultat = cache.get(key);
        AuthTokenInfo tokeinfo;

        if (resultat != null) {
            if (isTokenValid(resultat)) {
                return resultat;
            } else {
                tokeinfo = getTokenInfo();
                cache.put(key, tokeinfo);

                return tokeinfo;
            }

        } else {
            tokeinfo = getTokenInfo();
            cache.put(key, tokeinfo);
            return tokeinfo;
        }
    }

    public AuthTokenInfo getTokenInfo() {
        if (!isTokenValid(tokenInfo)) {
            this.setSecureServerUri(this.url + TOKEN_URL);
            this.tokenInfo = sendTokenRequest();
            return this.tokenInfo;
        }
        return tokenInfo;
    }

    public void setTokenInfo(AuthTokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public boolean isTokenValid(AuthTokenInfo token) {
        if (token != null) {
            Date now = new Date();
            Long tim = now.getTime();
            return token.getExpireTime() > tim;
        } else {
            return false;
        }
    }

    private TrustManager[] getTrustMgr() {
        TrustManager[] certs = {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String t) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String t) {
            }
        }};
        return certs;
    }

    private HttpHeaders getHeadersWithClientCredentials() {
        String credentials = this.clientID + ":" + this.plainClientCredentials;
        System.out.println(credentials);
        String base64ClientCredentials = new String(Base64.encodeBase64(credentials.getBytes()));

        System.out.println(base64ClientCredentials);
        HttpHeaders headers = getHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", "Basic " + base64ClientCredentials);
        return headers;
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactoryxxxxxx() {
        try {
            SSLContext ssl_ctx = SSLContext.getInstance("TLS");
            TrustManager[] trust_mgr = getTrustMgr();
            ssl_ctx.init(null, trust_mgr, new SecureRandom());
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ssl_ctx, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            return requestFactory;
        } catch (NoSuchAlgorithmException e) {

        } catch (KeyManagementException e) {

        }
        return null;
    }

    private Registry<ConnectionSocketFactory> socketConnection(SSLConnectionSocketFactory csf) {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", csf)
                .build();
        return registry;
    }

    private Registry<ConnectionSocketFactory> socketConnection() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .build();
        return registry;
    }

    private PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> registry) {
        PoolingHttpClientConnectionManager cManager = new PoolingHttpClientConnectionManager(registry);
        cManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // set maximum amount of connections for each http route in pool
        cManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        return cManager;
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactoryHttp() {
        try {

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(getPoolingHttpClientConnectionManager(socketConnection()))
                    .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                    .setRetryHandler(retryHandler())
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return requestFactory;
        } catch (Exception e) {

        }
        return null;
    }

    private static HttpRequestRetryHandler retryHandler() {
        return (exception, executionCount, context) -> {

            System.out.println("try request: " + executionCount);

            if (executionCount >= 3) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof InterruptedIOException) {
                // Timeout
                return false;
            }
            if (exception instanceof UnknownHostException) {
                // Unknown host
                return false;
            }
            if (exception instanceof SSLException) {
                // SSL handshake exception
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return true;
            }
            return false;
        };
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactory() {
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();
            System.out.println("ENTRER -----------------TLS---------------------");
            SSLContext ssl_ctx = SSLContext.getInstance("TLS");
            TrustManager[] trust_mgr = getTrustMgr();
            ssl_ctx.init(null, trust_mgr, new SecureRandom());
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ssl_ctx, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(getPoolingHttpClientConnectionManager(socketConnection(csf)))
                    .setDefaultRequestConfig(requestConfig)
                    .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                    .setSSLSocketFactory(csf)
                    .setRetryHandler(retryHandler())
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return requestFactory;
        } catch (NoSuchAlgorithmException e) {

        } catch (KeyManagementException e) {

        }
        return null;
    }

    private HttpHeaders getHeadersWithClientAuth(AuthTokenInfo tokenInfo) {
        HttpHeaders headers = getHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        if (tokenInfo != null) {
            if (tokenInfo.getToken_type() != null) {
                headers.add("Authorization", tokenInfo.getToken_type() + " " + tokenInfo.getAccess_token());
            } else {
                headers.add("Authorization", tokenInfo.getAccess_token());
            }
        }

        return headers;
    }

    public AuthTokenInfo sendTokenRequest() {
        // RestTemplate restTemplate = new RestTemplate();
        System.out.println("SECURE %%" + this.getSecureServerUri());
        String[] tablePrefix = this.secureServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;
        if (prefixeUrl.equals("http")) {
            restTemplate = new RestTemplate(getRequestFactoryHttp());
        } else {
            restTemplate = new RestTemplate(getRequestFactory());
        }

        HttpEntity<String> request = new HttpEntity<>(getHeadersWithClientCredentials());
        try {

            String access_token_ur = this.secureServerUri + this.grantUri;

            StringBuilder builder = new StringBuilder(access_token_ur);

            URI uri = URI.create(builder.toString());

            ResponseEntity<Object> response = restTemplate.exchange(uri, HttpMethod.POST, request, Object.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) response.getBody();
                AuthTokenInfo tokenInfo = null;
                if (map != null) {
                    Long expiresIn = System.currentTimeMillis();
                    tokenInfo = new AuthTokenInfo();
                    tokenInfo.setAccess_token((String) map.get("access_token"));
                    tokenInfo.setToken_type((String) map.get("token_type"));
                    tokenInfo.setRefresh_token((String) map.get("refresh_token"));
                    tokenInfo.setExpires_in((int) map.get("expires_in"));
                    tokenInfo.setScope((String) map.get("scope"));
                    expiresIn = expiresIn + (tokenInfo.getExpires_in() * 1000);
                    tokenInfo.setExpireTime(expiresIn);
                }
                return tokenInfo;
            } else {
                System.out.println(response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return null;
    }

    public RMessageResponse getPersonnesByTelephone(int page, int pageSize,
            String telephone,
            String reference,
            Long typeId,
            String codeTelephone) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des personnes .");
            this.setRestServerUri(this.url
                    + LISTE_PERSONNE_RECHERCHE_TELEPHONE);
            HashMap<String, Object> param = new HashMap();

            param.put("telephone", telephone);

            param.put("reference", reference);

            param.put("typeId", typeId);

            param.put("codeTelephone", codeTelephone);

            param.put("pageSize", pageSize);

            param.put("page", page);

            ResponseEntity result = this.getRessources(tokenInfo, param,
                    "GET", Optional.empty());

            if (result != null) {
                return (RMessageResponse) this.convert(result.getBody(),
                        RMessageResponse.class);
            }
            return null;
        } else {
            return null;
        }
    }

    public <T extends Object> T convert(Object body, Class<T> resultClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        T result = null;
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            String jsonInString = mapper.writeValueAsString(body);
            result = (T) (Object) mapper.readValue(jsonInString, resultClass);

        } catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    public <T extends StandardResponse> T sendDataRequest(AuthTokenInfo tokenInfo, Object donnee, Class<T> resultClass) throws InstantiationException, IllegalAccessException, IOException {
        String[] tablePrefix = restServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;
        T resultat;
        ResponseStatus responseStatus = new ResponseStatus();
        resultat = (T) (StandardResponse) resultClass.newInstance();
        if (prefixeUrl.equals("http")) {

            restTemplate = new RestTemplate(getRequestFactoryHttp());

        } else {
            restTemplate = new RestTemplate(getRequestFactory());
        }
        //    restTemplate.setErrorHandler( new RestTemplateErrorHandler());
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        ResponseEntity response = null;
        HttpMethod methode = HttpMethod.POST;
        try {

            HttpEntity<Object> request;
            if (tokenInfo != null) {
                request = new HttpEntity<>(donnee, getHeadersWithClientAuth(tokenInfo));

            } else {
                request = new HttpEntity<>(headers);
            }

            response = restTemplate.exchange(restServerUri, methode, request, Object.class);
            if (response.getStatusCodeValue() >= 400) {
                responseStatus.setStatusCode("" + response.getStatusCodeValue());
                responseStatus.setMsg(response.getBody());
            } else {
                responseStatus.setStatusCode("200");
                responseStatus.setMsg(response.getBody());
                resultat = (T) (StandardResponse) convert(responseStatus.getMsg(), resultClass);
            }
        } catch (HttpClientErrorException e) {

            responseStatus.setStatusCode("" + ErreurConstants.HTTP_CLIENT_STATUS_ERROR);
            responseStatus.setMsg(e.getMessage());
            resultat.setResponseStatus(responseStatus);
            return resultat;
        } catch (HttpStatusCodeException e) {
            responseStatus.setStatusCode("" + ErreurConstants.HTTP_CODE_STATUS_ERROR);
            responseStatus.setMsg(e.getMessage());
            resultat.setResponseStatus(responseStatus);
            return resultat;
        } catch (ResourceAccessException e) {
            responseStatus.setStatusCode("" + ErreurConstants.HTTP_CODE_STATUS_ERROR);
            responseStatus.setMsg(e.getMessage());
            resultat.setResponseStatus(responseStatus);
            return resultat;
        }

        return resultat;
    }

    public ResponseEntity getRessourcesObject2(AuthTokenInfo tokenInfo, Object donnee, String typeAppel) {
        String[] tablePrefix = restServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;
        if (prefixeUrl.equals("http")) {
            restTemplate = new RestTemplate(getRequestFactoryHttp());
        } else {
            restTemplate = new RestTemplate(getRequestFactory());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        ResponseEntity response = null;
        HttpMethod methode = HttpMethod.POST;
        switch (typeAppel) {
            case "POST":
                methode = HttpMethod.POST;
                break;
            case "GET":
                methode = HttpMethod.GET;
                break;
            case "PUT":
                methode = HttpMethod.PUT;
                break;
        }
        try {

            HttpEntity<Object> request;
            if (tokenInfo != null) {
                request = new HttpEntity<>(donnee, getHeadersWithClientAuth(tokenInfo));

            } else {
                request = new HttpEntity<>(headers);
            }

            response = restTemplate.exchange(restServerUri, methode, request, byte[].class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.HTTP_CLIENT_STATUS_ERROR, e.getMessage(), "0", null, null));
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.HTTP_CODE_STATUS_ERROR, e.getMessage(), "0", null, null));
        } catch (ResourceAccessException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.UNKNOWN_ERROR, "Ressource non disponible: erreur de connexion ou erreur non déterminée:erreur spécifique" + e.getMessage(), "0", null, null));
        }

        return response;
    }

    public ResponseEntity getRessourcesObject(AuthTokenInfo tokenInfo, Object donnee, String typeAppel) {
        String[] tablePrefix = restServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;
        if (prefixeUrl.equals("http")) {
            restTemplate = new RestTemplate(getRequestFactoryHttp());
        } else {
            restTemplate = new RestTemplate(getRequestFactory());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        ResponseEntity response = null;
        HttpMethod methode = HttpMethod.POST;
        switch (typeAppel) {
            case "POST":
                methode = HttpMethod.POST;
                break;
            case "GET":
                methode = HttpMethod.GET;
                break;
            case "PUT":
                methode = HttpMethod.PUT;
                break;
        }
        try {

            HttpEntity<Object> request;
            if (tokenInfo != null) {
                request = new HttpEntity<>(donnee, getHeadersWithClientAuth(tokenInfo));

            } else {
                request = new HttpEntity<>(headers);
            }
            response = restTemplate.exchange(restServerUri, methode, request, Object.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.HTTP_CLIENT_STATUS_ERROR, e.getMessage(), "0", null, null));
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.HTTP_CODE_STATUS_ERROR, e.getMessage(), "0", null, null));
        } catch (ResourceAccessException e) {
            return ResponseEntity.ok(enreResultat(ErreurConstants.UNKNOWN_ERROR, "Ressource non disponible: erreur de connexion ou erreur non déterminée:erreur spécifique" + e.getMessage(), "0", null, null));
        }

        return response;
    }

    public ResponseEntity getRessourcesFile(AuthTokenInfo tokenInfo, MultipartFile fichier, HashMap<String, Object> parametre) throws IOException {
        String[] tablePrefix = this.restServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;
        FileOutputStream fo;
        if (prefixeUrl.equals("http")) {
            restTemplate = new RestTemplate(getRequestFactoryHttp());
        } else {
            restTemplate = new RestTemplate(this.getRequestFactory());
        }
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String tempFileName = "./IN_FILE/" + fichier.getOriginalFilename();
        fo = new FileOutputStream(tempFileName);
        fo.write(fichier.getBytes());
        fo.close();
        map.add("file", new FileSystemResource(tempFileName));
        map.add("codePays", parametre.get("codePays"));
        map.add("compteDebit", parametre.get("compteDebit"));
        map.add("description", parametre.get("description"));
        map.add("numeroTelephone", parametre.get("numeroTelephone"));
        map.add("motifOperation", parametre.get("motifOperation"));
        map.add("typeTransfert", parametre.get("typeTransfert"));
        map.add("montantEntrp", parametre.get("montantEntrp"));

        HttpHeaders headers = new HttpHeaders();
        if (tokenInfo != null) {
            headers = this.getHeadersWithClientAuth(tokenInfo);
        }
        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename("Filename")
                .build();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setContentDisposition(contentDisposition);

        //    headers.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity response = null;
        HttpMethod methode = HttpMethod.POST;
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<String> respons = restTemplate.postForEntity(this.restServerUri, requestEntity, String.class);

        return respons;
    }

    public ResponseEntity getRessources(AuthTokenInfo tokenInfo, HashMap<String, Object> param, String typeAppel, Optional<HashMap<String, String>> lheaders) {
        String[] tablePrefix = this.restServerUri.split(":");
        String prefixeUrl = tablePrefix[0];
        RestTemplate restTemplate = null;

        if (prefixeUrl.equals("http")) {
            restTemplate = new RestTemplate(getRequestFactoryHttp());
        } else {
            restTemplate = new RestTemplate(this.getRequestFactory());
        }

        HttpHeaders headers = new HttpHeaders();

        // restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity response = null;
        HttpMethod methode = HttpMethod.POST;
        byte var11 = -1;
        switch (typeAppel.hashCode()) {
            case 70454:
                if (typeAppel.equals("GET")) {
                    var11 = 1;
                }
                break;
            case 79599:
                if (typeAppel.equals("PUT")) {
                    var11 = 2;
                }
                break;
            case 2461856:
                if (typeAppel.equals("POST")) {
                    var11 = 0;
                }
        }

        switch (var11) {
            case 0:
                methode = HttpMethod.POST;
                break;
            case 1:
                methode = HttpMethod.GET;
                break;
            case 2:
                methode = HttpMethod.PUT;
        }

        try {
            HttpEntity request;
            if (tokenInfo != null) {
                HttpHeaders headersToken = getHeadersWithClientAuth(tokenInfo);
                if (lheaders.isPresent()) {
                    for (Map.Entry mapentry : lheaders.get().entrySet()) {

                        headersToken.set(mapentry.getKey().toString(), mapentry.getValue().toString());

                    }
                }

                request = new HttpEntity(headersToken);

            } else {
                request = new HttpEntity(headers);
            }
            System.out.println("URISERVEUR=====" + this.restServerUri);

            response = restTemplate.exchange(this.restServerUri, methode, request, Object.class, param);

        } catch (HttpClientErrorException var12) {
            return ResponseEntity.ok(this.enreResultat(-300, var12.getMessage(), "0", (Object) null, (String) null));
        } catch (HttpStatusCodeException var13) {
            return ResponseEntity.ok(this.enreResultat(-200, var13.getMessage(), "0", (Object) null, (String) null));
        } catch (ResourceAccessException var14) {
            return ResponseEntity.ok(this.enreResultat(-400, "Ressource non disponible: erreur de connexion ou erreur non déterminée:erreur spécifique" + var14.getMessage(), "0", (Object) null, (String) null));
        }

        return response;
    }

    public ResultMessage enreResultat(int code, String msg, String couche, Object responseEntity, String responseType) {
        ResultMessage res = new ResultMessage();
        res.setErrCode(code);
        res.setMsg(msg);
        res.setNumCouche(couche);
        res.setResponseType(responseType);
        res.setResponseEntity(responseEntity);
        return res;
    }

    public List<MchargementTransfert> getChargement(String telephone, String code, String statut) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste chargement.");
            this.setRestServerUri(this.url + LIVE_FILE_LISTE_CHARGEMENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("code", code);
            param.put("statut", statut);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MchargementTransfert> fo = new ArrayList();
            List<MchargementTransfert> list = (List<MchargementTransfert>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return null;

    }

    public Page<MOperationDTO> getListOperation(
            String telephone,
            Long typeServiceId,
            Long canalId,
            Long uoAcquereurId,
            BigDecimal montantOp,
            String statutOperation,
            ZonedDateTime dateOperation,
            ZonedDateTime dateFin,
            String codeOperation,
            Long organisationId
    ) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste chargement.");
            this.setRestServerUri(this.url + LIVE_FILE_LISTE_OPERATION_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("typeServiceId", typeServiceId);
            param.put("canalId", canalId);
            param.put("uoAcquereurId", uoAcquereurId);
            param.put("statutOperation", statutOperation);
            param.put("dateOperation", dateOperation);
            param.put("dateFin", dateFin);
            param.put("codeOperation", codeOperation);
            param.put("organisationId", organisationId);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MchargementTransfert> fo = new ArrayList();
            ParameterizedTypeReference<Page<MOperationDTO>> responseType = new ParameterizedTypeReference<Page<MOperationDTO>>() {
            };
            Page<MOperationDTO> list = (Page<MOperationDTO>) this.convert(result.getBody(), responseType.getClass());
            return list;
        }
        return null;

    }

    public void updateUserFailure(String login) throws IllegalAccessException, IOException, InstantiationException {
        log.debug("GET : Liste chargement.");
        this.setRestServerUri(this.url + "/api/users/update_nbr_failure?login={login}");
        HashMap<String, Object> param = new HashMap();
        param.put("login", login);
        this.getRessources(tokenInfo, param, "GET", Optional.empty());

    }

    public ResponseEntity valideChargement(Long id, String user, String canal) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : vlidation chargement.");
            this.setRestServerUri(this.url + LIVE_VALIDE_CHARGEMENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);
            param.put("user", user);
            param.put("canal", canal);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            return result;

        }
        return null;

    }

    public ResponseEntity getFile(MultipartFile file, String codePays, String numeroTelephone, String compte, String description, String motifOperation, int typeTransfert, Double montantEntrp) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MEcran> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Chargement du fichier.");
            this.setRestServerUri(this.url + LIVE_FILE_TRANSFERT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codePays", codePays);
            param.put("compteDebit", compte);
            param.put("motifOperation", motifOperation);
            param.put("description", description);
            param.put("numeroTelephone", numeroTelephone);
            param.put("typeTransfert", typeTransfert);
            param.put("montantEntrp", montantEntrp);
            System.out.println("montantEntrp" + montantEntrp);
            System.out.println("PARAM MONTANT" + param.get("montantEntrp"));
            String type = file.getContentType();
            ResponseEntity result = this.getRessourcesFile(tokenInfo, file, param);
            MchargementTransfert fo = new MchargementTransfert();
            return result;

            //     MchargementTransfert list = (MchargementTransfert) this.convert(result.getBody(), fo.getClass());
        }
        return null;

    }

    public MResponse doCreateuser(MUser mUser) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation création d'un user.");
            this.setRestServerUri(this.url + LIVE_USER_CREATE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, mUser, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible de créer l'utilisateur.");
            }
        } else {
            return new MResponse("-1", "Impossible de créer l'utilisateur.");
        }
    }

    public MResponse doCreateReclamation(MReclamation mreclamation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("enregistrement d'une reclamation.");
            this.setRestServerUri(this.url + LIVE_RECLA_CREATE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, mreclamation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible de créer l'utilisateur.");
            }
        } else {
            return new MResponse("-1", "Impossible de créer l'utilisateur.");
        }
    }

    public MResponse doDesactiveClient(MPersonne mPersonne) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation desactivation.");
            this.setRestServerUri(this.url + LIVE_CLIENT_DESACTIVE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, mPersonne, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "impossble de desactiver.");
            }
        } else {
            return new MResponse("-1", "impossble de desactiver.");
        }
    }

    public MResponse doDesactiveAgent(MPersonne mPersonne) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation desactivation.");
            this.setRestServerUri(this.url + LIVE_AGENT_DESACTIVE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, mPersonne, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "impossble de desactiver.");
            }
        } else {
            return new MResponse("-1", "impossble de desactiver.");
        }
    }

    public MResponse deleteEntryDesactive(String id) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation suppression de la desactivation.");
            this.setRestServerUri(this.url + LIVE_DELETE_ENTRY_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse fo = new MResponse();
            MResponse user = (MResponse) this.convert(result.getBody(), fo.getClass());

            return user;

        }
        return null;

    }

    public StandardResponse getRequestCardless(String telephone, String reference, String montant, String entityCode) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Request for cardless.");
            this.setRestServerUri(this.url + LIVE_REQUEST_CARDLESS_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("reference", reference);
            param.put("montant", montant);
            param.put("entityCode", entityCode);
            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            StandardResponse fo = new StandardResponse();
            StandardResponse resultat = (StandardResponse) this.convert(result.getBody(), fo.getClass());

            return resultat;

        }
        return null;

    }

    public StandardResponse getRequestCardlessNew(String telephone, String reference, String montant, String entityCode, String codeFse) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Request for cardless.");
            this.setRestServerUri(this.url + LIVE_REQUEST_CARDLESS_NEW_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("reference", reference);
            param.put("montant", montant);
            param.put("entityCode", entityCode);
            param.put("codeFse", codeFse);
            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            StandardResponse fo = new StandardResponse();
            StandardResponse resultat = (StandardResponse) this.convert(result.getBody(), fo.getClass());
            return resultat;

        }
        return null;

    }

    public StandardResponse getRequestStatusCardless(String reference, String entityCode, String codeFse) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Request for cardless.");
            this.setRestServerUri(this.url + LIVE_REQUEST_STATUS_CARDLESS_NEW_URL);
            HashMap<String, Object> param = new HashMap();

            param.put("reference", reference);

            param.put("entityCode", entityCode);
            param.put("codeFse", codeFse);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            StandardResponse fo = new StandardResponse();
            StandardResponse resultat = (StandardResponse) this.convert(result.getBody(), fo.getClass());
            return resultat;

        }
        return null;

    }

    public MResponse changePassword(String login, String password) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation suppression de la desactivation.");
            this.setRestServerUri(this.url + LIVE_CHANGE_PASSWORD_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("login", login);
            param.put("password", password);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse fo = new MResponse();
            MResponse user = (MResponse) this.convert(result.getBody(), fo.getClass());

            return user;

        }
        return null;

    }

    public void updateUsr_nbre_failure() throws IllegalAccessException, IOException, InstantiationException {

        log.debug("Realisation vérification de la desactivation.");
        this.setRestServerUri(this.url + "/api/users/update_nbr_failure");
        HashMap<String, Object> param = new HashMap();
        this.getRessources(tokenInfo, param, "GET", Optional.empty());

    }

    public MDataReturn getTempPass(String data) throws IllegalAccessException, IOException, InstantiationException {
        log.debug("get temp pass.");
        this.setRestServerUri(this.url + "/api/users/temps_pass2?hostId={hostId}");
        AuthTokenInfo tokenInfo = getTokenInfo();
        HashMap<String, Object> param = new HashMap();
        param.put("hostId", data);
        ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
        MDataReturn temp = (MDataReturn) this.convert(result.getBody(), MDataReturn.class);
        return temp;

    }

    public MDataReturn verifToken(String token, String loginData) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation vérification de la desactivation.");
            this.setRestServerUri(this.url + "/api/machines/verif_tokenpti?token={token}&loginData={loginData}");
            HashMap<String, Object> param = new HashMap();
            param.put("token", token);
            param.put("loginData", loginData);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MDataReturn ok = (MDataReturn) this.convert(result.getBody(), MDataReturn.class);
            return ok;

        }
        return null;

    }

    public String isAuthentificationForte() throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation vérification de la desactivation.");
            this.setRestServerUri(this.url + LIVE_LIST_IS_AUTH_FORTE_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            String isAuth = this.convert(result.getBody(), String.class);
            return isAuth;

        }
        return null;

    }

    public Long getTauxChange(String devDepart, String devArrivee, String sens) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation vérification de la desactivation.");
            this.setRestServerUri(this.url + LIVE_LIST_TAUX_CHANGE_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("devDepart", devDepart);
            param.put("devArrivee", devArrivee);
            param.put("sens", sens);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

            Long taux = this.convert(result.getBody(), Long.class);
            return taux;

        }
        return null;

    }

    public UserExist verificationClient(String codePays, String login, String password) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation vérification de la desactivation." + login);
            this.setRestServerUri(this.url + LIVE_VERIFE_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codePays", codePays);
            param.put("login", login);
            param.put("password", password);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            UserExist fo = new UserExist();
            UserExist user = (UserExist) this.convert(result.getBody(), fo.getClass());
            return user;

        }
        return null;

    }

    /**
     * Service permettant de faire un appel de fonds à travers un compte
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationAccountCash(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation Account Cash.");
            operation.setTypeService(TypeService.ACCOUNT_CASH.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doPaiementInternet(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation paiement internet.");
            operation.setTypeService(TypeService.PAIEMENT_INTERNET.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    /**
     * Service permettant de faire un appel de fonds à travers un compte
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationRetraitCardLess(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation  cardless.");
            operation.setTypeService(TypeService.RETRAIT_CARDLESS.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    /**
     * Service permettant de faire un appel de fonds à travers un compte
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationPaiementFacture(MOperationFacture operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation Paiement facture.");
            operation.setTypeService(TypeService.PAIEMENT_FACTURE.toString());
            this.setRestServerUri(this.url + LIVE_OP_FACTURE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    /**
     * Service permettant de faire un appel de fonds à travers un compte
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationPaiementCredit(MOperationFacture operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation Paiement facture.");
            operation.setTypeService(TypeService.PAIEMENT_AIRTIME.toString());
            this.setRestServerUri(this.url + LIVE_OP_CREDIT_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationPaiementFactureCASH(MOperationFacture operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation paiement facture cash");
            operation.setTypeService(TypeService.PAIEMENT_FACTURE_DEPUIS_CASH.toString());
            this.setRestServerUri(this.url + LIVE_OP_FACTURE_CASH_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationEnvoiCash(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation envoi Cash.");
            operation.setTypeService(TypeService.ENVOI_CASH.toString());
            this.setRestServerUri(this.url + LIVE_OP_ENVOI_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationCashAccount(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation Cash Account .");
            operation.setTypeService(TypeService.CASH_ACCOUNT.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationCashAccountAgent(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation Account Cash.");
            operation.setTypeService(TypeService.CASH_ACCOUNT_AGENT.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public List<MEcran> getEcran(Long id) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MEcran> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des ecrans.");
            this.setRestServerUri(this.url + LIVE_LIST_ECRAN_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MEcran> fo = new ArrayList();
            List<MEcran> list = (List<MEcran>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public MClient getClientExist(String telephone, String code) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("GET : Liste des ecrans.");
            this.setRestServerUri(this.url + LIVE_IF_EXIT_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("code", code);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MClient fo = new MClient();
            MClient mclient = (MClient) this.convert(result.getBody(), fo.getClass());
            return mclient;

        }
        return null;

    }

    public List<MFacturier> getOrganisationTelephonie(String codeFse) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des téléphonies.");
            this.setRestServerUri(this.url + LIVE_LIST_FACTURIER_TEL_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeFse", codeFse);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MFacturier> fo = new ArrayList();
            List<MFacturier> list = (List<MFacturier>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public List<MFacturier> getFacturierOfOrganisation(String codeUo) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des téléphonies.");
            this.setRestServerUri(this.url + LIVE_FACTURIER_OFUO_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MFacturier> fo = new ArrayList();
            List<MFacturier> list = (List<MFacturier>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public MOrganisation getOrganisationByCode(String codeUo) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : .");
            this.setRestServerUri(this.url + LIVE_GET_UO_BYCODE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

            MOrganisation org = (MOrganisation) this.convert(result.getBody(), MOrganisation.class);
            return org;

        }
        return null;

    }

    public MOrganisation getOrganisationByCodePV(String codePv) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : .");
            this.setRestServerUri(this.url + LIVE_GET_UO_BYCODEPV_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codePv", codePv);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

            MOrganisation org = (MOrganisation) this.convert(result.getBody(), MOrganisation.class);
            return org;

        }
        return null;

    }

    public List<MOrganisation> getListOrganisationFils(String codeUo, String type) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MOrganisation> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des autorisations. organiation");
            this.setRestServerUri(this.url + LIVE_LIST_ORGANISATION_FILS);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            param.put("type", type);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MOrganisation> fo = new ArrayList();
            List<MOrganisation> list = (List<MOrganisation>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    //Recuperation du reseau entreprise
    //Recuperation du reseau entreprise
    public List<MOrganisation> getEntrepriseReseau(String telephone, String codePays) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des pays disponible sur pti.");
            this.setRestServerUri(this.url + LIST_RESEAU_ENTREPRISE);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("codePays", codePays);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MOrganisation> fo = new ArrayList();
            List<MOrganisation> list = (List<MOrganisation>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    //test imprime recu
    public HashMap<String, byte[]> getInfosRecu(MListParametreProduit mparametre) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Recherche Infos.");
            this.setRestServerUri(this.url + GET_INFO_RECU);
            HashMap<String, byte[]> classP = new HashMap();
            ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
            HashMap<String, byte[]> elem = (HashMap) this.convert(result.getBody(), classP.getClass());
            return elem;
        } else {
            return null;
        }
    }

    public List<MAutorisation> getAutotisationOrganisation(String codeUo) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MAutorisation> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des autorisations. organiation");
            this.setRestServerUri(this.url + LIVE_LIST_AUTORISATION_ORGANISATION);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MAutorisation> fo = new ArrayList();
            List<MAutorisation> list = (List<MAutorisation>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public List<MAutorisation> getAutotisationClient(String codeTelephone, String telephone) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MAutorisation> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : Liste des autorisations client.");
            this.setRestServerUri(this.url + LIVE_LIST_AUTORISATION_CLIENT);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("code", codeTelephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MAutorisation> fo = new ArrayList();
            List<MAutorisation> list = (List<MAutorisation>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    /**
     * **
     * Cette fonction permet de retourner les facturiers
     *
     * @param id
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public List<MFacturier> getFacturier(Long id) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            HashMap<String, Object> param = new HashMap();

            this.setRestServerUri(this.url + LIVE_LIST_FACTURIER_BASE_URL);

            param.put("id", id);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MFacturier> fo = new ArrayList();
            List<MFacturier> list = (List<MFacturier>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    /**
     * On prend en compte le canal
     *
     * @param id
     * @param canal
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public List<MFacturier> getFacturier(Long id, String canal) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            HashMap<String, Object> param = new HashMap();
            if ((canal == null) || canal.isEmpty() || canal.length() == 0) {
                this.setRestServerUri(this.url + LIVE_LIST_FACTURIER_BASE_URL);
            } else {
                this.setRestServerUri(this.url + LIVE_LIST_FACTURIER_CANAL_BASE_URL);
                param.put("canal", canal);
            }

            param.put("id", id);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MFacturier> fo = new ArrayList();
            List<MFacturier> list = (List<MFacturier>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public List<MProduit> getProduitFacturier(Long id) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MProduit> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_LIST_PRODUIT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MProduit> fo = new ArrayList();
            List<MProduit> list = (List<MProduit>) this.convert(result.getBody(), fo.getClass());
            return list;
        }
        return mResponse;
    }

    public List<MProduit> getProduitMarchand(String codeUo) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MProduit> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_LIST_PRODUIT_MRC_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MProduit> fo = new ArrayList();
            List<MProduit> list = (List<MProduit>) this.convert(result.getBody(), fo.getClass());
            return list;
        }
        return mResponse;
    }

    public List<MfactureMarchand> getFactureMarchand(Long id) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MfactureMarchand> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_LIST_FACTURE_MRC_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MfactureMarchand> fo = new ArrayList();
            List<MfactureMarchand> list = (List<MfactureMarchand>) this.convert(result.getBody(), fo.getClass());
            return list;
        }
        return mResponse;
    }

    public List<MClassificationFacturier> getListeClassification(String codeFse) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MClassificationFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_LIST_CLASS_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", codeFse);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MClassificationFacturier> fo = new ArrayList();
            List<MClassificationFacturier> list = (List<MClassificationFacturier>) this.convert(result.getBody(), fo.getClass());
            return list;
        }
        return mResponse;

    }

    public List<MTransaction> getListeOperationAttente(String valeurCompte, String typeCompte, String codeAgence, String codeFseTeneur, String typeServ) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MTransaction> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_OPERATION_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("valeurCompte", valeurCompte);
            param.put("typeCompte", typeCompte);
            param.put("codeAgence", codeAgence);
            param.put("codeFseTeneur", codeFseTeneur);
            param.put("typeServ", typeServ);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MTransaction> fo = new ArrayList();
            List<MTransaction> list = (List<MTransaction>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public List<MOperationNumbLastestDTO> getLastOperationAttente(String codeIndicatif, String telephone) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MOperationNumbLastestDTO> mResponse = new ArrayList();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_LAST_OPERATION_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", codeIndicatif);
            param.put("telephone", telephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MOperationNumbLastestDTO> fo = new ArrayList();
            List<MOperationNumbLastestDTO> list = (List<MOperationNumbLastestDTO>) this.convert(result.getBody(), fo.getClass());
            return list;

        }
        return mResponse;

    }

    public MResponse doOperationPaiement(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation de retrait.");
            operation.setTypeService(TypeService.PAIEMENT_BIEN.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_COMPTE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationTransfertVersCompte(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation cash à compte .");
            operation.setTypeService(TypeService.RECEPTION_TRANSFERT.toString());
            this.setRestServerUri(this.url + LIVE_OP_TRANSFERT_COMPTE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationRetraitCompte(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation de retrait.");
            operation.setTypeService(TypeService.RETRAIT_COMPTE.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_COMPTE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationCashACompte(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation de retrait.");
            operation.setTypeService(TypeService.CASH_ACCOUNT.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_COMPTE_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationApprovisionnement(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation d'approvisionnement.");
            operation.setTypeService(TypeService.APPROVISIONNEMENT_INTERNE.toString());
            this.setRestServerUri(this.url + LIVE_OP_APPROVISIONNEMENT);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationApprovisionnementEx(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation d'approvisionnement.");
            operation.setTypeService(TypeService.APPROVISIONNEMENT_EXTERNE.toString());
            this.setRestServerUri(this.url + LIVE_OP_APPROVISIONNEMENT);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationVenteDepositCardLess(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation d'approvisionnement.");
            operation.setTypeService(TypeService.VENTE_DEPOSIT_CARDLESS.toString());
            this.setRestServerUri(this.url + LIVE_OP_APPROVISIONNEMENT);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationRetraitCash(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation de retrait.");
            operation.setTypeService(TypeService.RETRAIT.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_CASH_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    /**
     * Service permettant de faire une operation de retrait suite à un appel de
     * fonds
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationRetrait(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation de retrait.");
            operation.setTypeService(TypeService.RETRAIT.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationSweepToAccount(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.SWEEP_DEPOSIT_TO_BANK.toString());
            this.setRestServerUri(this.url + LIVE_SWEEP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationAuToAllocation(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.AUTO_ALLOCATION.toString());
            this.setRestServerUri(this.url + LIVE_SWEEP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationWalleToCard(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(operation.getTypeService());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    /**
     * Service permettant de faire un virement de compte à compte
     *
     * @param operation
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doOperationAccountToAccount(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.ACCOUNT_ACCOUNT.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationBankToWallet(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.BANK_TO_WALLET.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doOperationWalletToBank(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.WALLET_TO_BANK.toString());
            this.setRestServerUri(this.url + LIVE_OP_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MRetraitResponse verifDonneeRetrait(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        MRetraitResponse mRes = new MRetraitResponse();
        MResponse re = new MResponse("-1", "Impossible d'effectuer l'operation.");
        mRes.setReponse(re);
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            operation.setTypeService(TypeService.ACCOUNT_ACCOUNT.toString());
            this.setRestServerUri(this.url + LIVE_VERIF_RETRAIT_BASE_URL);
            MRetraitResponse mResponse = sendDataRequest(tokenInfo, operation, MRetraitResponse.class);
            return mResponse;
        }
        return mRes;
    }

    /**
     * Service permettant de faire la souscription des comptes d'un client
     *
     * @param client
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MResponse doSouscriptionTemporaire(MClient client) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Souscription temporaire d'un client.");
            this.setRestServerUri(this.url + LIVE_SOUSCRIPTION_BASE_URL);
            MResponse mResponse = sendDataRequest(tokenInfo, client, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public ElementFacture getMontantFacture(MListParametreProduit mparametre) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Inscription du montant facture.");
            this.setRestServerUri(this.url + LIVE_MONTANT_FACTURE);
            ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
            ElementFacture elem = (ElementFacture) this.convert(result.getBody(), ElementFacture.class);
            return elem;
        } else {
            return null;
        }
    }

    public List<MParametre> getRechercheInfos(MListParametreProduit mparametre) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Recherche Infos.");
            this.setRestServerUri(this.url + GET_INFO);
            List<MParametre> classP = new ArrayList();
            ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
            List<MParametre> elem = (List<MParametre>) this.convert(result.getBody(), classP.getClass());
            return elem;
        } else {
            return null;
        }
    }

    public byte[] getPrint(MListParametreProduit mparametre) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            this.setRestServerUri(this.url + GET_PRINT);
            ResponseEntity result = this.getRessourcesObject2(tokenInfo, mparametre, "POST");
            byte[] list = (byte[]) this.convert(result.getBody(), byte[].class);
            return list;

        }
        return null;
    }

    public String getDataInfos(MListParametreProduit mparametre) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Recherche des données de la liste.");
            this.setRestServerUri(this.url + GET_DATA);
            List<MParametre> classP = new ArrayList();
            ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
            System.out.println("POST : Recherche des données de la liste." + result.getBody().toString());
            String strReturn = "";
            String strReturnD = "";
            List<MParametre> elem = (List<MParametre>) this.convert(result.getBody(), classP.getClass());
            Iterator it = elem.iterator();
            if (elem.size() > 0) {

                int i = 0;
                while (it.hasNext()) {
                    MParametre mParametre = (MParametre) this.convert(it.next(), MParametre.class);
                    strReturnD = mParametre.getCodeParametre() + ":" + mParametre.getValeur();
                    if (i == 0) {
                        strReturn = strReturnD;
                    } else {
                        strReturn = strReturn + ";" + strReturnD;
                    }
                    i = i + 1;
                }

            }
            System.out.println("POST : Recherche des données de la liste." + strReturn);
            return strReturn;
        } else {
            return null;
        }
    }

    public List<FactureFournisseur> getFacture(MListParametreProduit mparametre) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        Long montant = 0L;
        if (tokenInfo != null) {
            System.out.println("POST : LIST du montant facture.");
            this.setRestServerUri(this.url + LIVE_LIST_FACTURE_FOURNISSEUR);
            ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
            List<FactureFournisseur> fo = new ArrayList();
            List<FactureFournisseur> list = (List<FactureFournisseur>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public RegleFacturier getRegle(String reference, String montant, String idFacturier) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : REGLE DE GESTION");
            this.setRestServerUri(this.url + LIVE_REGLE_VERIF_FOURN_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("reference", reference);
            param.put("montant", montant);
            param.put("idFacturier", idFacturier);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<Mouvement> fo = new ArrayList();
            RegleFacturier regle = (RegleFacturier) this.convert(result.getBody(), RegleFacturier.class);
            return regle;
        } else {
            return null;
        }
    }

    /**
     * Solde d'un compte
     *
     * @param agence
     * @param numeroCompte
     * @param codeFse
     * @param acteur
     * @param identifiant
     * @param codeIndicatif
     * @return
     * @throws IOException
     */
    public Balance getSoldeCompte(String agence, String numeroCompte, String codeFse, String acteur, String identifiant, String codeIndicatif) throws IOException {

        long tempsDebut, tempsFin;

        long duree;
        tempsDebut = System.currentTimeMillis();
        AuthTokenInfo tokenInfo = tokenCache();
        tempsFin = System.currentTimeMillis();
        duree = tempsFin - tempsDebut;
        System.out.println("duree du token " + duree);
        if (tokenInfo != null) {
            System.out.println("GET : Liste des fournisseurs de services d'un pays.");
            this.setRestServerUri(this.url + COMPTE_SOLDE);
            HashMap<String, Object> param = new HashMap();
            param.put("agence", agence);
            param.put("acteur", acteur);
            param.put("identifiant", identifiant);
            param.put("codeIndicatif", codeIndicatif);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

            Balance fo = new Balance();

            Balance balance = (Balance) this.convert(result.getBody(), fo.getClass());

            return balance;
        } else {
            return null;
        }
    }

    public StoppageData getStoppageCompte(String agence, String numeroCompte, String codeFse, String montant, String sens, String eventId, String isInit) throws IOException {

        AuthTokenInfo tokenInfo = tokenCache();

        if (tokenInfo != null) {
            System.out.println("GET : Liste des fournisseurs de services d'un pays.");
            this.setRestServerUri(this.url + LIVE_GET_STOPPAGE);
            HashMap<String, Object> param = new HashMap();
            param.put("agence", agence);
            param.put("sens", sens);
            param.put("eventId", eventId);
            param.put("isInit", isInit);
            param.put("montant", montant);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            StoppageData fo = new StoppageData();
            StoppageData stoppage = (StoppageData) this.convert(result.getBody(), fo.getClass());

            return stoppage;
        } else {
            return null;
        }
    }

    public MResponseOrga doInscriptionTerminal(MTerminal terminal) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        MResponseOrga mResponse = new MResponseOrga();
        mResponse.setCode("-1");
        mResponse.setMessage("Impossible d'effectuer l'operation.");
        if (tokenInfo != null) {
            System.out.println("POST : Inscription du termminal.");
            this.setRestServerUri(this.url + LIVE_INCRIRE_TERMINAL);
            mResponse = sendDataRequest(tokenInfo, terminal, MResponseOrga.class);
            if (mResponse != null) {
                return mResponse;
            } else {

                return mResponse;
            }
        } else {
            return mResponse;
        }
    }

    public List<MTypeReclamation> getTypeReclamation() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des type reclamations.");
            this.setRestServerUri(this.url + LIVE_RECLA_LIST_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MTypeReclamation> fo = new ArrayList();
            List<MTypeReclamation> list = (List<MTypeReclamation>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    /**
     * @param telephone
     * @param code
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public List<MCompte> getComptesOfClient(String telephone, String code) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_COMPTES_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            param.put("telephone", telephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MCompte> fo = new ArrayList();
            List<MCompte> list = (List<MCompte>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public MCompteAgent getComptesOfAgent(String codeUo) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des comptes d'un client.");
            this.setRestServerUri(this.url + LIVE_COMPTES_AGENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MCompteAgent fo = new MCompteAgent();
            MCompteAgent cpt = (MCompteAgent) this.convert(result.getBody(), fo.getClass());
            return cpt;
        } else {
            return null;
        }
    }

    public MPersonne getInfosActeur(String telephone, String code, String codeActeur, String acteur) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des informations d'un client.");
            this.setRestServerUri(this.url + LIVE_INFOS_ACTEUR_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            param.put("telephone", telephone);
            param.put("acteur", acteur);
            param.put("codeActeur", codeActeur);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            System.out.println("BODY " + result.getBody());

            MPersonne personnes = (MPersonne) this.convert(result.getBody(), MPersonne.class);
            return personnes;
        } else {
            return null;
        }
    }

    public MOrganisation getInfosActeur(String codeActeur) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des informations d'une organisation.");
            this.setRestServerUri(this.url + LIVE_INFOS_ORGA_BASE_URL);
            HashMap<String, Object> param = new HashMap();

            param.put("codeActeur", codeActeur);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            System.out.println("BODY " + result.getBody());

            MOrganisation organ = (MOrganisation) this.convert(result.getBody(), MOrganisation.class);
            return organ;
        } else {
            return null;
        }
    }

    /**
     * *
     */
    public List<MPersonne> getInfosClient(String telephone, String code) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des informations d'un client.");
            this.setRestServerUri(this.url + LIVE_INFOS_CLIENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            param.put("telephone", telephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            System.out.println("BODY " + result.getBody());
            List<MPersonne> mPersonne = new ArrayList<>();
            List<MPersonne> personnes = (List<MPersonne>) this.convert(result.getBody(), mPersonne.getClass());
            return personnes;
        } else {
            return null;
        }
    }

    public InfosClientResponse getInfosClientx(String telephone, String code) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des informations d'un client.");
            this.setRestServerUri(this.url + LIVE_INFOS_CLIENT_BASE_URL);

            // Préparer les paramètres pour l'appel API
            HashMap<String, Object> param = new HashMap<>();
            param.put("code", code);
            param.put("telephone", telephone);

            // Effectuer l'appel REST et obtenir la réponse
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            System.out.println("BODY " + result.getBody());

            // Désérialisation du JSON dans un objet `InfosClientResponse`
            InfosClientResponse infosClientResponse = (InfosClientResponse) this.convert(
                    result.getBody(),
                    InfosClientResponse.class
            );

            return infosClientResponse;
        } else {
            return null;
        }
    }

    public List<MLocalisationOrganisation> getLocalisationOrganisation(String codeFse, String minLat, String maxLat, String minLon, String maxLon) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des pays disponible sur pti.");
            this.setRestServerUri(this.url + LIVE_LIST_POINT_VENTE_LOCALISATION);
            HashMap<String, Object> param = new HashMap();
            param.put("codeFse", codeFse);
            param.put("minLat", minLat);
            param.put("maxLat", maxLat);
            param.put("minLon", minLon);
            param.put("maxLon", maxLon);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MLocalisationOrganisation> fo = new ArrayList();
            List<MLocalisationOrganisation> list = (List<MLocalisationOrganisation>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    /**
     * Liste des pays disponible sur la plateforme PTI
     *
     * @return
     * @throws IOException
     */
    public List<MOrganisation> getPays() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des pays disponible sur pti.");
            this.setRestServerUri(this.url + LIVE_PAYS_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MOrganisation> fo = new ArrayList();
            List<MOrganisation> list = (List<MOrganisation>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public MDataParameter getDataParameter(String code) throws IOException, InstantiationException, IllegalAccessException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des données.");
            if ((code == null) || code.isEmpty() || code.length() == 0) {
                this.setRestServerUri(this.url + LIVE_DATA_PARAMETERS);
            } else {
                this.setRestServerUri(this.url + LIVE_DATA_PARAMETERS_PAYS);
            }
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            //    MDataParameter donnee = sendDataRequest(tokenInfo, null, MDataParameter.class);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            //   MDataParameter fo = new MDataParameter();
            MDataParameter donnee = (MDataParameter) this.convert(result.getBody(), MDataParameter.class);
            return donnee;
        } else {
            return null;
        }
    }

    public MDataTransaction getDataTransaction(String telephone, String code, String canal, String typeService, String codePv,
            String codeUosrc, String codeUoDest, BigDecimal montant, Optional<String> codeacteur) throws IOException, InstantiationException, IllegalAccessException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        String codeact = null;
        if (tokenInfo != null) {
            System.out.println("GET : Liste des données de transaction.");
            if (codeacteur.isPresent()) {
                this.setRestServerUri(this.url + LIVE_TRANS_PARAMETERS_ACTEUR);
                codeact = codeacteur.get();
            } else {
                this.setRestServerUri(this.url + LIVE_TRANS_PARAMETERS);
            }
            HashMap<String, Object> param = new HashMap();
            param.put("canal", canal);
            param.put("typeService", typeService);
            param.put("codePv", codePv);
            param.put("codeUoSrc", codeUosrc);
            param.put("codeUoDest", codeUoDest);
            param.put("montant", montant);
            param.put("code", code);
            /* code indicatif */
            param.put("telephone", telephone);
            param.put("codeacteur", codeact);
            //    MDataParameter donnee = sendDataRequest(tokenInfo, null, MDataParameter.class);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            //   MDataParameter fo = new MDataParameter();
            MDataTransaction donnee = (MDataTransaction) this.convert(result.getBody(), MDataTransaction.class);
            return donnee;
        } else {
            return null;
        }
    }

    public MDataTransaction getDataTransaction(String codeFse, String telephone, String code, String canal, String typeService, String codePv,
            String codeUosrc, String codeUoDest, BigDecimal montant, String typeCompte, String valeurCompte, String codeAgence, String fseTeneur, Optional<String> codeacteur) throws IOException, InstantiationException, IllegalAccessException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        String codeact = null;
        if (tokenInfo != null) {
            System.out.println("GET : Liste des données de transaction.");
            if (codeacteur.isPresent() && !codeacteur.get().equals("0")) {
                this.setRestServerUri(this.url + EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS_ACTEUR);
                codeact = codeacteur.get();
            } else {
                this.setRestServerUri(this.url + EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS);
            }
            HashMap<String, Object> param = new HashMap();
            param.put("canal", canal);
            param.put("typeService", typeService);
            param.put("codePv", codePv);
            param.put("codeUoSrc", codeUosrc);
            param.put("codeUoDest", codeUoDest);
            param.put("montant", montant);
            param.put("valeurCompte", valeurCompte);
            param.put("codeAgence", codeAgence);
            param.put("typeCompte", typeCompte);
            param.put("teneur", fseTeneur);
            param.put("code", code);
            param.put("codeFse", codeFse);
            /* code indicatif */
            param.put("telephone", telephone);
            param.put("codeacteur", codeact);
            //    MDataParameter donnee = sendDataRequest(tokenInfo, null, MDataParameter.class);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            //   MDataParameter fo = new MDataParameter();
            MDataTransaction donnee = (MDataTransaction) this.convert(result.getBody(), MDataTransaction.class);
            return donnee;
        } else {
            return null;
        }
    }

    /**
     * Liste des motifs disponible sur PTI
     *
     * @return
     * @throws IOException
     */
    public List<MtypeDoc> getTypeDOcument() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des type documents disponible sur pti.");
            this.setRestServerUri(this.url + LIVE_TYPEDOC_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MtypeDoc> typedoc = new ArrayList();
            List<MtypeDoc> list = (List<MtypeDoc>) this.convert(result.getBody(), typedoc.getClass());
            return list;
        } else {
            return null;
        }
    }

    public List<MTypePiece> getTypePiece() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des type piece sur pti.");
            this.setRestServerUri(this.url + LIVE_TYPEPIECE_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MTypePiece> typPiece = new ArrayList();
            List<MTypePiece> list = (List<MTypePiece>) this.convert(result.getBody(), typPiece.getClass());
            return list;
        } else {
            return null;
        }
    }

    public MPointVente getCodeAgentGab(String codePays) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Code Agent GAB.");
            this.setRestServerUri(this.url + LIVE_AGENT_GAB_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codePays", codePays);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MPointVente fo = new MPointVente();
            // String agentGab =result.getBody().toString();
            MPointVente agentGab = (MPointVente) this.convert(result.getBody(), fo.getClass());
            return agentGab;
        } else {
            return null;
        }
    }

    public List<MFaq> getFaq() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des faq disponible sur pti.");
            this.setRestServerUri(this.url + LIVE_FAQ_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MFaq> fo = new ArrayList();
            List<MFaq> list = (List<MFaq>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    /**
     * Liste des motifs disponible sur PTI
     *
     * @return
     * @throws IOException
     */
    public List<MMotif> getMotifs() throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des motifs disponible sur pti.");
            this.setRestServerUri(this.url + LIVE_MOTIF_URL);
            HashMap<String, Object> param = new HashMap();
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MMotif> fo = new ArrayList();
            List<MMotif> list = (List<MMotif>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public MResponse doVerifPointVenteIsAffected(String codeActeur, String codePv) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de l'affectation ");
            this.setRestServerUri(this.url + LIVE_VERIFPV_BASE_URL);

            HashMap<String, Object> param = new HashMap();
            param.put("codeActeur", codeActeur);
            param.put("codePv", codePv);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MResponse doAnnulation(String codeOperation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de l'affectation ");
            this.setRestServerUri(this.url + LIVE_ANNULATION_OPERATION);
            HashMap<String, Object> param = new HashMap();
            param.put("codeOperation", codeOperation);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MResponse verifPersonneInscrit(String telephone, String code, String email, String qualif) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de la personne ");
            this.setRestServerUri(this.url + LIVE_PERSONNE_MORALE_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("telephone", telephone);
            param.put("code", code);
            param.put("code", code);
            param.put("email", email);
            param.put("qualif", qualif);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MResponse verifCompteIfExist(String numeroCompte, String acteur, String codeIndicatif, String identifiant) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de la validité d'un compte ");
            this.setRestServerUri(this.url + LIVE_COMPTE_VERIF_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("numeroCompte", numeroCompte);
            param.put("acteur", acteur);
            param.put("codeIndicatif", codeIndicatif);
            param.put("identifiant", identifiant);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MRetraitResponse getDetailOperation(String codeOperation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        MRetraitResponse mRes = new MRetraitResponse();
        MResponse re = new MResponse("-1", "Impossible d'effectuer l'operation.");
        mRes.setReponse(re);
        if (tokenInfo != null) {

            System.out.println("GET :detail de l'operation " + codeOperation);
            this.setRestServerUri(this.url + LIVE_BLOCAGE_TRANS_DETAIL_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeoperation", codeOperation);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MRetraitResponse mResponse = (MRetraitResponse) this.convert(result.getBody(), MRetraitResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return mRes;
            }
        } else {
            return mRes;
        }
    }

    public MResponse doBlocage(String codeOperation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : blocage d'une transaction " + codeOperation);
            this.setRestServerUri(this.url + LIVE_BLOCAGE_TRANS_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeoperation", codeOperation);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MResponse doAffectationPV(String codeActeur, String codePv, String login) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de l'affectation ");
            this.setRestServerUri(this.url + LIVE_AFFECTPV_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeActeur", codeActeur);
            param.put("codePv", codePv);
            param.put("login", login);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public List<MPointVente> getPointVente(String code) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des points de vente.");
            this.setRestServerUri(this.url + LIVE_LISTPV_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MPointVente> fo = new ArrayList();
            List<MPointVente> list = (List<MPointVente>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    /**
     * Liste des fournisseurs de services disponible dans un pays
     *
     * @param code
     * @return
     * @throws IOException
     */
    public List<MOrganisation> getFseOfPays(String code) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des fournisseurs de services d'un pays.");
            this.setRestServerUri(this.url + LIVE_FSE_OF_PAYS);
            HashMap<String, Object> param = new HashMap();
            param.put("code", code);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MOrganisation> fo = new ArrayList();
            List<MOrganisation> list = (List<MOrganisation>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    /**
     * Service de calcul de frais
     *
     * @param canal : le canal de communication
     * @param typeService
     * @param codePv Le code du point de vente
     * @param codeUoDest
     * @param codeUosrc
     * @param montant
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    public MFrais calculFrais(String canal, String typeService, String codePv,
            String codeUoDest, String codeUosrc, BigDecimal montant) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : Calcul des frais de l'operation.");
            this.setRestServerUri(this.url + LIVE_CALCUL_FRAIS);
            HashMap<String, Object> param = new HashMap();
            param.put("canal", canal);
            param.put("typeService", typeService);
            param.put("codePv", codePv);
            param.put("codeUoSrc", codeUosrc);
            param.put("codeUoDest", codeUoDest);
            param.put("montant", montant);
          
            
            
            
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MFrais frais = new MFrais();
            MFrais f = (MFrais) this.convert(result.getBody(), frais.getClass());
            if (f != null) {
                f.setResponseStatus(new ResponseStatus(STATUS_CODE, MSG));
                return f;
            } else {
                return f;
            }
        } else {
            return null;
        }
    }

    public Message verificationOtp(String otp, String identifiant, String codeFse) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        Message msg = null;
        if (tokenInfo != null) {
            System.out.println("POST : LIVE_VERIF_CODE_OTP.");
            HashMap<String, Object> param = new HashMap();
            param.put("otp", otp);
            param.put("identifiant", identifiant);
            param.put("codeFse", codeFse);
            this.setRestServerUri(this.url + LIVE_VERIF_CODE_OTP);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            msg = (Message) this.convert(result.getBody(), Message.class);
        }
        return msg;
    }

    public Message getGenereOTP(String agence, String numeroCompte, String codeFse, String teneur, Long codeProduit) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : infos client.");
            this.setRestServerUri(this.url + LIVE_GEN_CODE_OTP);
            HashMap<String, Object> param = new HashMap();
            param.put("agence", agence);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);
            param.put("teneur", teneur);
            param.put("codeProduit", codeProduit);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            Message msg = new Message();
            Message message = (Message) this.convert(result.getBody(), msg.getClass());
            return message;
        } else {
            return null;
        }
    }

    public CustomerInformation getInfosClientCompte(String agence, String numeroCompte, String codeFse) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : infos client.");
            this.setRestServerUri(this.url + COMPTE_INFOSCLIENT);
            HashMap<String, Object> param = new HashMap();
            param.put("agence", agence);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            CustomerInformation cus = new CustomerInformation();
            CustomerInformation customer = (CustomerInformation) this.convert(result.getBody(), cus.getClass());
            return customer;
        } else {
            return null;
        }
    }

    /**
     * Historique des transactions d'un compte
     *
     * @param agence
     * @param numeroCompte
     * @param codeFse
     * @param debutPeriode
     * @param finPeriode
     * @param acteur
     * @param identifiant
     * @param codeIndicatif
     * @param isUoAdmin
     * @return
     * @throws IOException
     */
    public List<Mouvement> getHistoriqueCompte(String agence, String numeroCompte, String codeFse, String debutPeriode, String finPeriode, String acteur, String identifiant, String codeIndicatif, String pointVente, String isUoAdmin) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des transactions d'un compte.");
            this.setRestServerUri(this.url + COMPTE_HISTORIQUE_TRANSACTION);
            HashMap<String, Object> param = new HashMap();
            HashMap<String, String> header = new HashMap();
            header.put("POINTVENTE", pointVente);
            header.put("ISUOADMIN", isUoAdmin);
            param.put("agence", agence);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);
            param.put("debutPeriode", debutPeriode);
            param.put("finPeriode", finPeriode);
            param.put("acteur", acteur);
            param.put("identifiant", identifiant);
            param.put("codeIndicatif", codeIndicatif);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.of(header));
            List<Mouvement> fo = new ArrayList();
            List<Mouvement> list = (List<Mouvement>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public List<Mouvement> getMiniReleve(String agence, String numeroCompte, String codeFse, String acteur, String identifiant, String codeIndicatif, String pointVente, String isUoAdmin) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des transactions d'un mini releve");
            this.setRestServerUri(this.url + COMPTE_MINI_RELEVE);
            HashMap<String, Object> param = new HashMap();
            HashMap<String, String> header = new HashMap();
            header.put("POINTVENTE", pointVente);
            header.put("ISUOADMIN", isUoAdmin);
            param.put("agence", agence);
            param.put("numeroCompte", numeroCompte);
            param.put("codeFse", codeFse);
            param.put("acteur", acteur);
            param.put("identifiant", identifiant);
            param.put("codeIndicatif", codeIndicatif);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.of(header));
            List<Mouvement> fo = new ArrayList();
            List<Mouvement> list = (List<Mouvement>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public List<MNotification> getNotficiation(String numero, String codeIndicatif) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des notification d'un client.");
            this.setRestServerUri(this.url + LISTE_NOTIFICATION_TRANSACTION);
            HashMap<String, Object> param = new HashMap();
            param.put("numero", numero);
            param.put("codeIndicatif", codeIndicatif);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MNotification> fo = new ArrayList();
            List<MNotification> list = (List<MNotification>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public List<MPublicite> getPublicite(String codeIndicatif) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des publicite.");
            this.setRestServerUri(this.url + LIVE_GET_LIST_PUBLICITE);
            HashMap<String, Object> param = new HashMap();

            param.put("codeIndicatif", codeIndicatif);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<MPublicite> fo = new ArrayList();
            List<MPublicite> list = (List<MPublicite>) this.convert(result.getBody(), fo.getClass());
            return list;
        } else {
            return null;
        }
    }

    public RMessageResponse getAllOperation(int page, int pageSize,
            String typeServiceId,
            Long canalId,
            Long uoAcquereurId,
            BigDecimal montantOp,
            String statutOperation,
            ZonedDateTime dateOperation,
            ZonedDateTime dateFin,
            String codeOperation,
            Long organisationId,
            String utilisateur) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : Liste des Operations .");
            this.setRestServerUri(this.url + LISTE_OPERATION_ALL_TRANSACTION);
            HashMap<String, Object> param = new HashMap();

            param.put("typeServiceId", typeServiceId);

            param.put("canalId", canalId);

            param.put("uoAcquereurId", uoAcquereurId);

            param.put("montantOp", montantOp);

            param.put("statutOperation", statutOperation);

            param.put("codeOperation", codeOperation);

            param.put("dateOperation", dateOperation);

            param.put("dateFin", dateFin);

            param.put("organisationId", organisationId);

            param.put("utilisateur", utilisateur);

            param.put("pageSize", pageSize);

            param.put("page", page);

            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

            if (result != null) {

                RMessageResponse messageResponse = (RMessageResponse) this.convert(result.getBody(), RMessageResponse.class);

                return messageResponse;
            }

            return null;
        } else {
            return null;
        }
    }

    public ResponseEntity sendMail(String mail, String message) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : ENVOI DU MAIL.");
            this.setRestServerUri(this.url + SEND_MAIL);
            HashMap<String, Object> param = new HashMap();
            param.put("message", message);
            param.put("mail", mail);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            return result;
        } else {
            return null;
        }
    }

    /**
     * @param telephone
     * @param message
     * @return
     * @throws IOException
     */
    public ResponseEntity updateAliasPv(String alias, String codePv) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : ENVOI DU MESSAGE.");
            this.setRestServerUri(this.url + LIVE_MAJ_ALIAS_PV_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("alias", alias);
            param.put("codePv", codePv);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            return result;
        } else {
            return null;
        }
    }

    /**
     * @param telephone
     * @param message
     * @return
     * @throws IOException
     */
    public ResponseEntity sendSms(String telephone, String message) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : ENVOI DU MESSAGE.");
            this.setRestServerUri(this.url + SEND_SMS);
            HashMap<String, Object> param = new HashMap();
            param.put("message", message);
            param.put("telephone", telephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            return result;
        } else {
            return null;
        }
    }
    //****************************EXTERNAL***********************************************

    public CredentialApi verification(String params, String hashParam, String clientId) throws IOException {
        System.out.println("GET : External Verification");
        this.setRestServerUri(this.url + EXTERNAL_VERIFICATION);
        HashMap<String, Object> param = new HashMap();
        param.put("params", params);
        param.put("hashParam", hashParam);
        param.put("client_id", clientId);
        ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
        CredentialApi credentialApi = new CredentialApi();
        credentialApi = this.convert(result.getBody(), credentialApi.getClass());
        return credentialApi;
    }

    private void setParamCredentials(CredentialApi credentialApi) {
        this.clientID = credentialApi.getClient_id();
        this.plainClientCredentials = credentialApi.getClient_secret();
        this.password = credentialApi.getClient_secret();
        this.grantUri = "?grant_type=client_credentials&scope=read&client_secret=" + this.plainClientCredentials + "&client_id=" + this.clientID;
        System.out.println("grantUri " + this.grantUri);
    }

    public Message getSoldeExternal(String codePays, String telephone, String hashParam, String clientId) throws IOException {

        CredentialApi credentialApi = verification(codePays + telephone, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();

            if (tokenInfo != null) {
                System.out.println("GET : External getSolde");
                this.setRestServerUri(this.url + EXTERNAL_GET_SOLDE);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                Message message = new Message();
                message = this.convert(result.getBody(), message.getClass());
                return message;
            }
            Message erreur = new Message();
            erreur.setMsg("Erreur de connection au serveur .");
            return erreur;
        }
        Message erreur = new Message();
        erreur.setMsg("Parametres incorrect !");
        return erreur;
    }

    public Message getInfosClientExternal(String codePays, String telephone, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codePays + telephone, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : External getSolde");
                this.setRestServerUri(this.url + EXTERNAL_GET_INFOS_CLIENT);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                Message message = new Message();
                message = this.convert(result.getBody(), message.getClass());
                return message;
            }
        }
        Message erreur = new Message();
        erreur.setMsg("Erreur de connection au serveur .");
        return erreur;
    }

    public Message getHistoriqueExternal(String codePays, String telephone, String debutPeriode, String finPeriode, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codePays + telephone + debutPeriode + finPeriode, hashParam, clientId);
        Message erreur = new Message();
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : External getSolde");
                this.setRestServerUri(this.url + EXTERNAL_GET_HISTORIQUE);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                param.put("debutPeriode", debutPeriode);
                param.put("finPeriode", finPeriode);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                Message message = new Message();
                message = this.convert(result.getBody(), message.getClass());
                return message;
            }

            erreur.setMsg("Erreur de connection au serveur .");
            return erreur;

        }
        erreur.setMsg("Paramètres erronés !");
        return erreur;
    }

    public byte[] getPrintBordereau(EtatParams etatParams) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            this.setRestServerUri(this.url + "/api/reporting/operations/bordereau");
            ResponseEntity result = this.getRessourcesObject2(tokenInfo, etatParams, "POST");
            ModelAndView mo = new ModelAndView();
            byte[] list = (byte[]) this.convert(result.getBody(), byte[].class);
            return list;
        }
        return null;
    }

    public byte[] getPrintOperationBordereau(EtatParams etatParams) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            this.setRestServerUri(this.url + "/api/reporting/operations/caissier/listing");
            ResponseEntity result = this.getRessourcesObject2(tokenInfo, etatParams, "POST");
            ModelAndView mo = new ModelAndView();
            byte[] list = (byte[]) this.convert(result.getBody(), byte[].class);
            return list;
        }
        return null;
    }

    public MFrais calculFrais(String typeService,
            String codePv,
            String codeUoDest,
            String codeUoSrc,
            BigDecimal montant, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(typeService + codePv + codeUoDest + codeUoSrc + montant, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : External calcul frais");
                this.setRestServerUri(this.url + EXTERNAL_CALCUL_FRAIS);
                HashMap<String, Object> param = new HashMap();
                param.put("typeService", typeService);
                param.put("codePv", codePv);
                param.put("codeUoDest", codeUoDest);
                param.put("codeUoSrc", codeUoSrc);
                param.put("montant", montant);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                MFrais mFrais = new MFrais();
                mFrais = this.convert(result.getBody(), mFrais.getClass());
                return mFrais;
            }

        }
        return null;
    }

    public Message sendCodeOTP(String codePays, String telephone, String hashParam, String clientId) throws IOException {
        Message erreur = new Message();
        CredentialApi credentialApi = verification(codePays + telephone, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();

            if (tokenInfo != null) {
                System.out.println("TOKEN : " + tokenInfo.getAccess_token());
                System.out.println("POST : External SendCodeOTP");
                System.out.println("URL_PTI_CONNECT : " + this.url);
                this.setRestServerUri(this.url + EXTERNAL_SEND_CODE_OTP);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                Message message = new Message();
                message = this.convert(result.getBody(), message.getClass());
                return message;
            }
            erreur.setMsg("Erreur de connection au serveur .");
            return erreur;
        }
        erreur.setMsg("Paramètres erronés !");
        return erreur;
    }

    public ExternalResponse paiemantInternet(String codePays, String telephone, String codePv, String codeRetrait, String montant, String isMontant, String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codePays + telephone + codePv + codeRetrait + montant + isMontant, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant internet");
                this.setRestServerUri(this.url + EXTERNAL_PAIEMENT_INTERNET);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                param.put("codePv", codePv);
                param.put("codeRetrait", codeRetrait);
                param.put("montant", montant);
                param.put("isMontant", isMontant);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }
            erreur.setMessage("Erreur de connection au serveur !");
            return erreur;
        }
        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    /**
     * **************************
     */
    /**
     * **************************
     * @param codePv
     * @param numeroCompte
     * @param msisdn
     * @param montantOp
     * @param codeOperateur
     * @param referenceExterne
     * @param api
     * @param hashParam
     * @param clientId
     * @return
     * @throws java.io.IOException
     */
    public MResponse virementWallet(String codePv, String numeroCompte, String codePays, String msisdn, BigDecimal montantOp, String codeOperateur, String referenceExterne, String api, String hashParam, String clientId, String bankCible, String alias, String canal) throws IOException {
        CredentialApi credentialApi = verification(msisdn + numeroCompte, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = this.getTokenInfo();
            if (tokenInfo != null) {
                //if (Util.isBiggerThan(montantOp, BigDecimal.ZERO)) {
                this.setRestServerUri(this.url + LIVE_VIREMENT_EXT_WALLET_URL);
                HashMap<String, Object> param = new HashMap();
                param.put("numeroCompte", numeroCompte);
                param.put("codePv", codePv);
                param.put("codePays", codePays);
                param.put("msisdn", msisdn);
                param.put("montantOp", montantOp);
                param.put("referenceExterne", referenceExterne);
                param.put("codeOperateur", codeOperateur);
                param.put("canal", canal);
                param.put("bankCible", bankCible);
                param.put("api", api);
                param.put("alias", alias);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                MResponse response = this.convert(result.getBody(), MResponse.class);

                return response;
                ///  } else {
                //    return null;
                // }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public MResponse paiementWallet(String codePvSrc, String codePvDest, String numeroCompte, String beneficiaire, String donneurOrdre, BigDecimal montantOp, String codeOperateur, String referenceExterne, String hashParam, String clientId, String bankCible, String canal) throws IOException {
        CredentialApi credentialApi = verification(codePvSrc + codePvDest + numeroCompte + montantOp.longValue(), hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = this.getTokenInfo();
            if (tokenInfo != null) {
                //if (Util.isBiggerThan(montantOp, BigDecimal.ZERO)) {
                this.setRestServerUri(this.url + LIVE_PAIEMENT_MARCHAND_EXT_WALLET_URL);
                HashMap<String, Object> param = new HashMap();
                param.put("numeroCompte", numeroCompte);
                param.put("codePvSrc", codePvSrc);
                param.put("codePvDest", codePvDest);
                param.put("montantOp", montantOp);
                param.put("referenceExterne", referenceExterne);
                param.put("codeOperateur", codeOperateur);
                param.put("canal", canal);
                param.put("bankCible", bankCible);
                param.put("donneurOrdre", donneurOrdre);
                param.put("beneficiaire", beneficiaire);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                MResponse response = this.convert(result.getBody(), MResponse.class);

                return response;
                ///  } else {
                //    return null;
                // }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public MInfos getInfosAlias(String alias, String codeFse, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(alias, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : infos client.");
                this.setRestServerUri(this.url + COMPTE_INFOSCLIENT_ALIAS);
                HashMap<String, Object> param = new HashMap();
                param.put("alias", alias);
                param.put("codeFse", codeFse);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                MInfos cus = new MInfos();
                MInfos customer = (MInfos) this.convert(result.getBody(), cus.getClass());
                return customer;
            } else {
                return null;
            }
        }
        return null;
    }

    public CustomerInformation getInfosClientCompteEx(String agence, String numeroCompte, String codeFse, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(agence + numeroCompte + codeFse, hashParam, clientId);
        if (credentialApi != null && credentialApi.getClient_secret() != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : infos client.");
                this.setRestServerUri(this.url + COMPTE_INFOSCLIENT);
                HashMap<String, Object> param = new HashMap();
                param.put("agence", agence);
                param.put("numeroCompte", numeroCompte);
                param.put("codeFse", codeFse);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                CustomerInformation cus = new CustomerInformation();
                CustomerInformation customer = (CustomerInformation) this.convert(result.getBody(), cus.getClass());
                return customer;
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    public Balance getSoldeWallet(String numeroCompte, String agence, String msisdn, String codeOperateur, String bankCible, String hashParam, String clientId, String alias) throws IOException {

        CredentialApi credentialApi = verification(msisdn + numeroCompte, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();

            if (tokenInfo != null) {
                System.out.println("GET : External getSolde");
                this.setRestServerUri(this.url + "/api/v1/operations/solde?numeroCompte={numeroCompte}&agence={agence}&msisdn={msisdn}&codeOperateur={codeOperateur}&bankCible={bankCible}&alias={alias}");
                HashMap<String, Object> param = new HashMap();
                param.put("numeroCompte", numeroCompte);
                param.put("agence", agence);
                param.put("msisdn", msisdn);
                param.put("codeOperateur", codeOperateur);
                param.put("bankCible", bankCible);
                param.put("alias", alias);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                Balance fo = new Balance();
                Balance balance = (Balance) this.convert(result.getBody(), fo.getClass());
                return balance;

            }

            return null;
        }

        return null;
    }

    public MResponse dodeLink(ExtBwCustomer extBwCustomer, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {
        CredentialApi credentialApi = verification(extBwCustomer.getMsisdn() + extBwCustomer.getNumeroCompte(), hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : Souscription temporaire d'un client.");
                this.setRestServerUri(this.url + LIVE_DELINK_URL);
                MResponse mResponse = sendDataRequest(tokenInfo, extBwCustomer, MResponse.class);
                if (mResponse != null) {
                    return mResponse;
                } else {
                    return new MResponse("-1", "Impossible d'effectuer l'operation.");
                }
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Paramètres erronés.");
        }

    }

    public MResponse doLink(ExtBwCustomer extBwCustomer, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {
        CredentialApi credentialApi = verification(extBwCustomer.getMsisdn() + extBwCustomer.getNumeroCompte(), hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : Souscription temporaire d'un client.");
                this.setRestServerUri(this.url + LIVE_LINK_URL);
                MResponse mResponse = sendDataRequest(tokenInfo, extBwCustomer, MResponse.class);
                if (mResponse != null) {
                    return mResponse;
                } else {
                    return new MResponse("-1", "Impossible d'effectuer l'operation.");
                }
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Paramètres erronés.");
        }
    }

    public ExternalResponse depotCompte(String codePaysRecepteur,
            String telephoneRecepteur,
            String codePv,
            BigDecimal montant,
            String codeOTP, String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codePaysRecepteur + telephoneRecepteur + codePv + montant, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External depot");
                this.setRestServerUri(this.url + EXTERNAL_PAIEMENT_DEPOT);
                HashMap<String, Object> param = new HashMap();
                param.put("codePaysRecepteur", codePaysRecepteur);
                param.put("telephoneRecepteur", telephoneRecepteur);
                param.put("codePv", codePv);
                param.put("montant", montant);
                param.put("clientId", clientId);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }

            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }
        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    /**
     * ***********************************************
     */
    public ExternalResponse paiemantBien(String codePays,
            String telephone,
            String codePv,
            BigDecimal montant,
            String codeOTP, String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codePays + telephone + codePv + montant + codeOTP, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant bien");
                this.setRestServerUri(this.url + EXTERNAL_PAIEMENT_BIEN);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                param.put("codePv", codePv);
                param.put("montant", montant);
                param.put("codeOTP", codeOTP);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }

            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }
        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    public ExternalResponse retraitCompte(String codePays,
            String telephone,
            String codePv,
            BigDecimal montant,
            String codeOTP, String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codePays + telephone + codePv + montant + codeOTP, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External retrait compte");
                this.setRestServerUri(this.url + EXTERNAL_RETRAIT_COMPTE);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                param.put("codePv", codePv);
                param.put("montant", montant);
                param.put("codeOTP", codeOTP);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }

            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }
        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    public ExternalResponse accountAccount(String codePaysEmetteur,
            String telephoneEmetteur,
            String codePaysRecepteur,
            String telephoneRecepteur,
            String codePv,
            BigDecimal montant,
            String codeOTP, String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codePaysEmetteur + telephoneEmetteur + codePaysRecepteur + telephoneRecepteur + codePv + montant + codeOTP, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant bien");
                this.setRestServerUri(this.url + EXTERNAL_ACCOUNT_ACCOUNT);
                HashMap<String, Object> param = new HashMap();
                param.put("codePaysEmetteur", codePaysEmetteur);
                param.put("telephoneEmetteur", telephoneEmetteur);
                param.put("codePaysRecepteur", codePaysRecepteur);
                param.put("telephoneRecepteur", telephoneRecepteur);
                param.put("codePv", codePv);
                param.put("montant", montant);
                param.put("codeOTP", codeOTP);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }
            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }

        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    public String convertToJsonX(Object body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(body);
        } catch (IOException e) {
        }
        return json;
    }

    public MResponse setAnnuler(String codeOperation, String codePv,
            String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codeOperation + codePv, hashParam, clientId);

        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant bien");
                this.setRestServerUri(this.url + EXTERNAL_OPERATION_ANNULATION);
                HashMap<String, Object> param = new HashMap();
                param.put("codeOperation", codeOperation);
                param.put("codePv", codePv);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                MResponse fo = new MResponse();
                MResponse mResponse = (MResponse) this.convert(result.getBody(), fo.getClass());
                return mResponse;
            }

        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }

        return new MResponse("-1", "Paramètres erronés .");
    }

    public ExternalResponse getOperationExterneStatus(String codeOperation,
            String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codeOperation, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant bien");
                this.setRestServerUri(this.url + EXTERNAL_CODE_OPERATION_STATUS);
                HashMap<String, Object> param = new HashMap();
                param.put("codeOperation", codeOperation);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }
            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }

        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    public ExternalResponse getOperationStatus(String codeOperation,
            String hashParam, String clientId) throws IOException {
        ExternalResponse erreur = new ExternalResponse();
        CredentialApi credentialApi = verification(codeOperation, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External paiemant bien");
                this.setRestServerUri(this.url + EXTERNAL_OPERATION_STATUS);
                HashMap<String, Object> param = new HashMap();
                param.put("codeOperation", codeOperation);
                ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                return response;
            }
            erreur.setMessage("Erreur de connection au serveur .");
            return erreur;
        }

        erreur.setMessage("Paramètres erronés !");
        return erreur;
    }

    public MResponse sendOperationMsg(String codeOperation) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : External Facturiers By Code UO");
            this.setRestServerUri(this.url + RESEND_MESSAGE_OPERATION);
            HashMap<String, Object> param = new HashMap();
            param.put("codeOperation", codeOperation);

            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            MResponse response = this.convert(result.getBody(), MResponse.class);
            return response;
        }
        return null;

    }

    public GrData getFacturiersByCodeUO(String codeUo, String secteur, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codeUo + secteur, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External Facturiers By Code UO");
                this.setRestServerUri(this.url + EXTERNAL_FACTURIERS_BY_CODE_UO);
                HashMap<String, Object> param = new HashMap();
                param.put("codeUo", codeUo);
                param.put("secteur", secteur);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                GrData response = this.convert(result.getBody(), GrData.class);
                return response;
            }
            return null;
        }
        return null;
    }

    public List<MParametre> getInfosFacturesExternal(MListParametreProduit mparametre, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {
        /*String mparametreStr = this.convertToJsonX(mparametre);
        CredentialApi credentialApi = verification(mparametreStr, hashParam, clientId);*/
        String mparametreStr = mparametre.getIdProduit() + "";
        System.out.println("mparametreStr " + mparametreStr);
        CredentialApi credentialApi = verification(mparametreStr, hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : Recherche Infos.");
                this.setRestServerUri(this.url + EXTERNAL_GET_INFOS_FACTURE);
                List<MParametre> classP = new ArrayList();
                ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
                List<MParametre> elem = (List<MParametre>) this.convert(result.getBody(), classP.getClass());
                return elem;
            } else {
                return null;
            }
        }
        return null;
    }

    public MFraisResponse getOperationFees(ExtCallData extCallData, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {
        //   String mparametreStr = this.convertToJsonX(mparametre);
        CredentialApi credentialApi = verification(extCallData.getCanal() + extCallData.getCodeFacturier() + extCallData.getCodeOperateur() + extCallData.getMontant(), hashParam, clientId);
        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : Recherche Infos.");
                this.setRestServerUri(this.url + EXTERNAL_GET_OPERATION_FEES);
                MFraisResponse classP = new MFraisResponse();
                ResponseEntity result = this.getRessourcesObject(tokenInfo, extCallData, "POST");
                MFraisResponse elem = (MFraisResponse) this.convert(result.getBody(), classP.getClass());
                return elem;
            } else {
                return null;
            }
        }
        return null;
    }

    public List<FactureFournisseur> getFactureExternal(MListParametreProduit mparametre, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {
//        String mparametreStr = this.convertToJsonX(mparametre);
//        String mparametreStr = mparametre.getIdProduit()+"";

        CredentialApi credentialApi = verification(mparametre.getIdProduit() + "", hashParam, clientId);

        if (credentialApi != null) {
            setParamCredentials(credentialApi);
            Long montant = 0L;
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : LIST du montant facture.");
                this.setRestServerUri(this.url + EXTERNAL_GET_LIST_FACTURES);
                ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
                List<FactureFournisseur> fo = new ArrayList();
                List<FactureFournisseur> list = (List<FactureFournisseur>) this.convert(result.getBody(), fo.getClass());
                return list;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    public RegleFacturier getRegleExternal(String reference, String montant, String idFacturier, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(reference + montant + idFacturier, hashParam, clientId);
        if (credentialApi != null) {
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : REGLE DE GESTION");
                this.setRestServerUri(this.url + LIVE_REGLE_VERIF_FOURN_BASE_URL);
                HashMap<String, Object> param = new HashMap();
                param.put("reference", reference);
                param.put("montant", montant);
                param.put("idFacturier", idFacturier);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                List<Mouvement> fo = new ArrayList();
                RegleFacturier regle = (RegleFacturier) this.convert(result.getBody(), RegleFacturier.class);
                return regle;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<MfactureMarchand> getListePaiementFacture(String codePays,
            String telephone, String codeFaturier, Long taille, Long page, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {

        List<MfactureMarchand> mResponse = new ArrayList();
        List<MfactureMarchand> fo = new ArrayList();
        CredentialApi credentialApi = verification(codePays + telephone + codeFaturier + taille + "" + page + "", hashParam, clientId);
        if (credentialApi != null) {
            System.out.println("GET :.");
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : Liste des comptes d'un client.");
                this.setRestServerUri(this.url + EXTERNAL_GET_LIST_PAIEMENT_FACTURE);
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                param.put("codeFaturier", codeFaturier);
                param.put("taille", taille);
                param.put("page", page);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                fo = new ArrayList();
                List<MfactureMarchand> list = (List<MfactureMarchand>) this.convert(result.getBody(), fo.getClass());
                return list;
            } else {
                return null;
            }
        }
        return null;
    }

    public MResponse doPaiementFacture(String codePays, String telephone, String codePv, BigDecimal montant, String codeFacturier, Long produitId,
            String canal, String codeFseSrc, String typeService, String fees, String referenceFacture, MParametrePaiementFacture mparametre, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi;
        String ref = "";

        if (referenceFacture != null) {
            ref = referenceFacture;
        }

        credentialApi = verification(codePays + telephone + codePv + montant + codeFacturier + "" + produitId + "" + canal + codeFseSrc + typeService + fees + ref, hashParam, clientId);

        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("GET : REGLE DE GESTION");
                String uriRefFacture = "";
                if (referenceFacture != null) {
                    uriRefFacture = "&referenceFacture=" + referenceFacture;
                }
                String uriPath;
                if (codeFseSrc != null) {
                    uriPath = "?codePays=" + codePays + "&telephone=" + telephone + "&codePv=" + codePv + "&montant=" + montant + "&codeFacturier=" + codeFacturier + "&produitId=" + produitId + "&canal=" + canal + "&codeFseSrc=" + codeFseSrc + "&typeService=" + typeService + "&fees=" + fees + uriRefFacture + "&hashParam=" + hashParam + "&clientId=" + clientId;
                } else {
                    uriPath = "?codePays=" + codePays + "&telephone=" + telephone + "&codePv=" + codePv + "&montant=" + montant + "&codeFacturier=" + codeFacturier + "&produitId=" + produitId + "&canal=" + canal + "&typeService=" + typeService + "&fees=" + fees + uriRefFacture + "&hashParam=" + hashParam + "&clientId=" + clientId;

                }

                this.setRestServerUri(this.url + LIVE_PAIEMENT_FACTURE + uriPath);
                HashMap<String, Object> param = new HashMap();

                ResponseEntity result = this.getRessourcesObject(tokenInfo, mparametre, "POST");
                List<Mouvement> fo = new ArrayList();
                MResponse response = (MResponse) this.convert(result.getBody(), MResponse.class);
                return response;
            } else {
                return new MResponse("-1", "Paramètre de connexion erronés");
            }
        } else {
            return new MResponse("-1", "Paramètre de connexion eronnés");
        }
    }

    public MResponse doExternalgetInfos(String accountId, String last4Digit, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(accountId + last4Digit, hashParam, clientId);

        if (credentialApi != null) {
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External Facturiers By Code UO");
                this.setRestServerUri(this.url + EXTRERNAL_CARD_URL + "/getInfos?accountId={accountId}&last4Digit={last4Digit}");
                HashMap<String, Object> param = new HashMap();
                param.put("accountId", accountId);
                param.put("last4Digit", last4Digit);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

                MClient response = this.convert(result.getBody(), MClient.class);
                if (response != null && response.getCompte() != null) {
                    MResponse mResponse = new MResponse();
                    mResponse.setCode("0");
                    mResponse.setmClient(response);
                    return mResponse;
                }
                return new MResponse("-1", "Impossible de retourné les infos de cette carte");
            }
            return new MResponse("-3", "Identifiant incorrect");
        }
        return new MResponse("-2", "Les données hachées ne sont pas conformes");

    }

    public Message doExternalgenereOTP(String codePays, String telephone, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codePays + telephone, hashParam, clientId);
        Message response = new Message();
        if (credentialApi != null) {
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External Facturiers By Code UO");
                this.setRestServerUri(this.url + EXTRERNAL_CARD_URL + "/genereOTP?codePays={codePays}&telephone={telephone}");
                HashMap<String, Object> param = new HashMap();
                param.put("codePays", codePays);
                param.put("telephone", telephone);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());

                response = this.convert(result.getBody(), Message.class);
                if (response != null) {
                    return response;
                }
                response = new Message();
                response.setErrCode("-1");
                response.setMsg("Impossible de générer le code");
                response.setCodeErr("-1");
                response.setText("Impossible de générer le code");
                return response;
            }
        }
        response = new Message();
        response.setErrCode("-1");
        response.setMsg("Les données hachées ne sont pas conformes");
        response.setCodeErr("-1");
        response.setText("Les données hachées ne sont pas conformes");
        return response;

    }

    public ExternalResponse doOperationRecharge(String codePays, String telephone, String codePv, String codeOperateur, String montant, String fees, String canal, ExtActeurTransfert extActeurTransfert, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codePays + telephone + codePv + codeOperateur + montant + fees + canal, hashParam, clientId);

        if (credentialApi != null) {
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External recharge");

                String uriPath = "/recharge?codePays=" + codePays + "&telephone=" + telephone + "&codePv=" + codePv + "&codeOperateur=" + codeOperateur + "&montant=" + montant + "&fees=" + fees + "&canal=" + canal;

                this.setRestServerUri(this.url + EXTRERNAL_CARD_URL + uriPath);

                ResponseEntity result = this.getRessourcesObject(tokenInfo, extActeurTransfert, "POST");

                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                if (response != null) {
                    return response;
                }
                return new ExternalResponse("-1", "Impossible de terminer l'opération. Veuillez réessayer plus tard", "", "");
            }
        }
        return new ExternalResponse("-1", "Les données hachées ne sont pas conformes", "", "");

    }

    public ExternalResponse doOperationLinkCard(String codePays, String telephone, String codeOTP, ExtBwCustomer extBwCustomer, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi = verification(codePays + telephone + codeOTP, hashParam, clientId);
        if (credentialApi != null) {
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External linkCard");

                String uriPath = "/link?codePays=" + codePays + "&telephone=" + telephone + "&codeOTP=" + codeOTP;

                this.setRestServerUri(this.url + EXTRERNAL_CARD_URL + uriPath);

                ResponseEntity result = this.getRessourcesObject(tokenInfo, extBwCustomer, "POST");

                ExternalResponse response = this.convert(result.getBody(), ExternalResponse.class);
                if (response != null) {
                    return response;
                }
                return new ExternalResponse("-1", "Impossible de terminer l'opération. Veuillez réessayer plus tard", "", "");
            }
        }
        return new ExternalResponse("-1", "Les données hachées ne sont pas conformes", "", "");

    }

    public MResponse linkageExternal(MOperationFacture mOperationFacture, String hashParam, String clientId) throws IOException {
        CredentialApi credentialApi;
        String ref = "";

        credentialApi = verification(mOperationFacture.getCodePv() + mOperationFacture.getFacturier().getId() + mOperationFacture.getTypeService() + mOperationFacture.getCanal() + mOperationFacture.getProduit().getId(), hashParam, clientId);

        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                System.out.println("POST : External linkCard");
                this.setRestServerUri(this.url + LIVE_LINK_WALLET);
                HashMap<String, Object> param = new HashMap();

                ResponseEntity result = this.getRessourcesObject(tokenInfo, mOperationFacture, "POST");
                List<Mouvement> fo = new ArrayList();
                MResponse response = (MResponse) this.convert(result.getBody(), MResponse.class);
                return response;
            } else {
                return new MResponse("-1", "Paramètre de connexion erronés");
            }
        } else {
            return new MResponse("-1", "Paramètre de connexion eronnés");
        }
    }

    //chatbot
    public ChatbotResponseDTO chatbotcomo(ChatbotRequestBodyDTO chatbotRequestBodyDTO) throws IOException {

        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : External linkCard");
            this.setRestServerUri(this.url + CHATBOT_COMO_BASE_URL);
            ResponseEntity result = this.getRessourcesObject(tokenInfo, chatbotRequestBodyDTO, "POST");
            ChatbotResponseDTO response = (ChatbotResponseDTO) this.convert(result.getBody(), ChatbotResponseDTO.class);
            return response;
        } else {
            return new ChatbotResponseDTO();
        }
    }
    //chatbot

    public MWallet listWalletOrCardExternal(String codeIndicatif, String msisdn, String accountNumber, String typeCompte, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {

        System.out.println("LIST " + typeCompte);
        CredentialApi credentialApi;
        MWallet response;
        credentialApi = verification(codeIndicatif + msisdn + accountNumber + typeCompte, hashParam, clientId);
        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                this.setRestServerUri(this.url + LIVE_LIST_WALLET + "?codeIndicatif={codeIndicatif}&msisdn={msisdn}&accountNumber={accountNumber}&typeCompte={typeCompte}");
                HashMap<String, Object> param = new HashMap();
                param.put("codeIndicatif", codeIndicatif);
                param.put("msisdn", msisdn);
                param.put("accountNumber", accountNumber);
                param.put("typeCompte", typeCompte);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                response = this.convert(result.getBody(), MWallet.class);
                if (response != null) {
                    return response;
                }
                return new MWallet("-1", "Impossible de retourner les donnees");
            } else {
                return new MWallet("-3", "Impossible d'effectuer l'operation.");
            }
        }
        return new MWallet("-2", "Donnees hachees non conforme.");
    }

    public MCard listCardExternal(String codeIndicatif, String msisdn, String accountNumber, String typeCompte, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {

        System.out.println("LIST " + typeCompte);
        CredentialApi credentialApi;
        MCard response;
        credentialApi = verification(codeIndicatif + msisdn + accountNumber + typeCompte, hashParam, clientId);
        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                this.setRestServerUri(this.url + LIVE_LIST_WALLET + "?codeIndicatif={codeIndicatif}&msisdn={msisdn}&accountNumber={accountNumber}&typeCompte={typeCompte}");
                HashMap<String, Object> param = new HashMap();
                param.put("codeIndicatif", codeIndicatif);
                param.put("msisdn", msisdn);
                param.put("accountNumber", accountNumber);
                param.put("typeCompte", typeCompte);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                response = this.convert(result.getBody(), MCard.class);
                if (response != null) {
                    return response;
                }
                return new MCard("-1", "Impossible de retourner les donnees");
            } else {
                return new MCard("-3", "Identifiant incorrect");
            }
        }
        return new MCard("-2", "Les données hachées ne sont pas conformes");
    }

    public MCard soldeCardExternal(String accountId, String last4Digit, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {

        System.out.println("SOLDE CARTE " + accountId);
        CredentialApi credentialApi;
        MCard response;
        credentialApi = verification(accountId + last4Digit, hashParam, clientId);
        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                this.setRestServerUri(this.url + LIVE_SOLDE_CARTE + "?accountId={accountId}&last4Digit={last4Digit}");
                HashMap<String, Object> param = new HashMap();
                param.put("accountId", accountId);
                param.put("msisdn", last4Digit);
                ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
                response = this.convert(result.getBody(), MCard.class);
                if (response != null) {
                    return response;
                }
                return new MCard("-1", "Impossible de retourner le solde de la carte");
            } else {
                return new MCard("-3", "Identifiant incorrect");
            }
        }
        return new MCard("-2", "Les données hachées ne sont pas conformes");
    }

    public MWallet getClientListWallet(MWalletList mWalletList, String hashParam, String clientId) throws IllegalAccessException, IOException, InstantiationException {

        System.out.println("LIST " + mWalletList.getTypeCompte());
        CredentialApi credentialApi;
        MWallet response;
        credentialApi = verification(mWalletList.getCodeIndicatif() + mWalletList.getMsisdn() + mWalletList.getTypeCompte(), hashParam, clientId);
        if (credentialApi != null) {
            this.setParamCredentials(credentialApi);
            AuthTokenInfo tokenInfo = getTokenInfo();
            if (tokenInfo != null) {
                this.setRestServerUri(this.url + LIVE_CLIENT_LIST_WALLET);
                ResponseEntity result = this.getRessourcesObject(tokenInfo, mWalletList, "POST");
                response = this.convert(result.getBody(), MWallet.class);
                if (response != null) {
                    return response;
                }
                return new MWallet("-1", "Impossible de retourner les donnees");
            } else {
                return new MWallet("-3", "Impossible d'effectuer l'operation.");
            }
        }
        return new MWallet("-2", "Donnees hachees non conforme.");
    }

//****************************EXTERNAL***********************************************
    public MResponse updateOperation(String codeOperation, String codeAgence, String codeBanque, String numeroCompte, String rib, String ref) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            MResponse mResponse;
            this.setRestServerUri(this.url + SAVE_OPERATION_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codeOperation", codeOperation);
            param.put("codeAgence", codeAgence);
            param.put("codeBanque", codeBanque);
            param.put("numeroCompte", numeroCompte);
            param.put("rib", rib);
            param.put("ref", ref);
            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            log.debug("--------result---{}", result);
            MOperationDTO op = (MOperationDTO) this.convert(result.getBody(), MOperationDTO.class);
            if (op != null && op.getCodeOperation() != null) {
                return new MResponse("0", "Enregistrement reussi", op.getCodeOperation());
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        }
        return null;
    }

    public List<MOperationPartenaire> getOperationPartenaire(String codePv, ZonedDateTime dateDebut, ZonedDateTime dateFin) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();

        if (tokenInfo != null) {
            List<MOperationPartenaire> mOperationDTO = new ArrayList();
            MResponse mResponse;
            this.setRestServerUri(this.url + LISTE_OPERATION_PARTENAIRE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("codePv", codePv);
            param.put("dateDebut", dateDebut);
            param.put("dateFin", dateFin);
            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            List<MOperationPartenaire> list = (List<MOperationPartenaire>) this.convert(result.getBody(), mOperationDTO.getClass());
            log.debug("--------list---{}", list);
            return list;
        }
        return null;
    }

    public Message virementRtgs(String codeFse, String compteDebiteur, String codeAgenceDebit, String referenceExterne, String compteCredit, String montantDebit, String codeAgenceCredit, String codeBanqueCredit, String codeBanqueDebit, String nomBeneficiaire, String nomDonneurOrdre, String description, String canal, String externe, String cle) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("POST : .");
            this.setRestServerUri(this.url + VIREMENT_RTGS_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("compteDebiteur", compteDebiteur);
            param.put("codeAgenceDebit", codeAgenceDebit);
            param.put("referenceExterne", referenceExterne);
            param.put("compteCredit", compteCredit);
            param.put("montantDebit", montantDebit);
            param.put("codeAgenceCredit", codeAgenceCredit);
            param.put("codeBanqueCredit", codeBanqueCredit);
            param.put("codeBanqueDebit", codeBanqueDebit);
            param.put("nomBeneficiaire", nomBeneficiaire);
            param.put("nomDonneurOrdre", nomDonneurOrdre);
            param.put("description", description);
            param.put("canal", canal);
            param.put("externe", externe);
            param.put("cle", cle);
            param.put("codeFse", codeFse);

            ResponseEntity result = this.getRessources(tokenInfo, param, "POST", Optional.empty());
            log.debug("--------result---{}", result);
            Message op = (Message) this.convert(result.getBody(), Message.class);
            if (op != null && op.getCodeErr() != null) {
                return op;
            }
        }
        return null;
    }

    public MResponse doExternalContexteOperation(MOperation operation, String codeFse, String typeService) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            log.debug("Realisation Paiement facture." + codeFse);
            operation.setTypeService(typeService);
            this.setRestServerUri(this.url + EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL + "/operations/doSettlement?codeFse=" + codeFse);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MResponse doExternalOperationPaiementFacture(MOperationFacture operation, String codeFse, String typeService) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation Paiement facture.");
            log.debug("Realisation Paiement facture." + codeFse);
            operation.setTypeService(typeService);
            this.setRestServerUri(this.url + EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL + "/operations/doSettlementFacture?codeFse=" + codeFse);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public GrData doExternalGetListeFacturier(String codeFse, String secteur) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : External Facturiers By Code UO");
            this.setRestServerUri(this.url + EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL + "/facturiers/liste-facturiers?codeFse={codeFse}&secteur={secteur}");
            HashMap<String, Object> param = new HashMap();
            param.put("codeFse", codeFse);
            param.put("secteur", secteur);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            GrData response = this.convert(result.getBody(), GrData.class);
            return response;
        }
        return null;

    }

    public GrData doExternalGetFacturierInfos(String codeUo, String secteur) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : External Facturiers By Code UO");
            this.setRestServerUri(this.url + EXTERNAL_CONTEXTE_GET_FACTURIER);
            HashMap<String, Object> param = new HashMap();
            param.put("codeUo", codeUo);
            param.put("secteur", secteur);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            GrData response = this.convert(result.getBody(), GrData.class);
            return response;
        }
        return null;

    }

    public List<ActiviteClientDTO> getListeActivite(String codeTelephone, String telephone) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("POST : External Facturiers By Code UO");
            List<ActiviteClientDTO> activiteClientDTO = new ArrayList();
            this.setRestServerUri(this.url + EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL + "/activite/liste-activite?codeTelephone={codeTelephone}&telephone={telephone}");
            HashMap<String, Object> param = new HashMap();
            param.put("codeTelephone", codeTelephone);

            param.put("telephone", telephone);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            List<ActiviteClientDTO> response = this.convert(result.getBody(), activiteClientDTO.getClass());
            return response;
        }
        return null;

    }

    public MDataTransaction getExternalContexteDataTransaction(String telephone, String code, String canal, String typeService, String codePv,
            String codeUosrc, String codeUoDest, BigDecimal montant, String typeCompte, String valeurCompte, String codeAgence, String fseTeneur, String codeFse, Optional<String> codeacteur) throws IOException, InstantiationException, IllegalAccessException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        String codeact = null;
        if (tokenInfo != null) {
            System.out.println("GET : Liste des données de transaction.");
            if (codeacteur.isPresent()) {
                this.setRestServerUri(this.url + EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS);
                codeact = codeacteur.get();
            } else {
                this.setRestServerUri(this.url + EXTERNAL_CONTEXTE_LIVE_TRANS_PARAMETERS_ACTEUR);
            }
            HashMap<String, Object> param = new HashMap();
            param.put("canal", canal);
            param.put("typeService", typeService);
            param.put("codePv", codePv);
            param.put("codeUoSrc", codeUosrc);
            param.put("codeUoDest", codeUoDest);
            param.put("montant", montant);
            param.put("valeurCompte", valeurCompte);
            param.put("codeAgence", codeAgence);
            param.put("typeCompte", typeCompte);
            param.put("teneur", fseTeneur);
            param.put("code", code);
            param.put("codeFse", codeFse);
            /* code indicatif */
            param.put("telephone", telephone);
            param.put("codeacteur", codeact);
            //    MDataParameter donnee = sendDataRequest(tokenInfo, null, MDataParameter.class);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            //   MDataParameter fo = new MDataParameter();
            MDataTransaction donnee = (MDataTransaction) this.convert(result.getBody(), MDataTransaction.class);
            return donnee;
        } else {
            return null;
        }
    }

    public MResponse doExternalOperationCardGetInfo(MOperationFacture operation, String codeFse, String typeService) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation Paiement facture.");
            operation.setTypeService(typeService);
            this.setRestServerUri(this.url + EXTRERNAL_CONTEXT_LIVE_OP_BASE_URL + "/operations/doSettlement/?codeFse=" + codeFse);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public ResponseEntity verificationChargement(Long id, String user, String canal)
            throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        List<MFacturier> mResponse = new ArrayList();
        if (tokenInfo != null) {
            log.debug("GET : verification chargement.");
            this.setRestServerUri(this.url + LIVE_VERIFICATION_CHARGEMENT_BASE_URL);
            HashMap<String, Object> param = new HashMap();
            param.put("id", id);
            param.put("user", user);
            param.put("canal", canal);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            return result;
        }
        return null;

    }

    public Message sendOtp(String codePays, String telephone, String identifiant) throws IOException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : REGLE DE GESTION");
            this.setRestServerUri(this.url + EXTERNAL_SEND_OTP);
            HashMap<String, Object> param = new HashMap();
            param.put("codePays", codePays);
            param.put("telephone", telephone);
            param.put("identifiant", identifiant);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            Message message = (Message) this.convert(result.getBody(), Message.class);
            return message;
        } else {
            return null;
        }

    }

    public MResponse doActionNumeroCardLessPV(String codePv, String codeActeur, String login, String action) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("GET : vérification de l'ajout ");
            this.setRestServerUri(this.url + AJOUT_RETRAIT_NUMERO_CARDLESS);
            HashMap<String, Object> param = new HashMap();
            param.put("codeActeur", codeActeur);
            param.put("codePv", codePv);
            param.put("login", login);
            param.put("action", action);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            MResponse mResponse = (MResponse) this.convert(result.getBody(), MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Erreur.");
            }
        } else {
            return new MResponse("-1", "Erreur.");
        }
    }

    public MResponse doOperationRetraitCardLessAgent(MOperation operation) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            log.debug("Realisation d'une operation  cardless.");
            operation.setTypeService(TypeService.VENTE_DEPOSIT_CARDLESS.toString());
            this.setRestServerUri(this.url + LIVE_OP_RETRAIT_CARDLESS_AGENT);
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }

    public MDataTransaction getDataTransactionInterOp(String telephone, String code, String canal, String typeService, String codePv,
            String codeUosrc, String codeUoDest, BigDecimal montant, String typeCompte, String valeurCompte, String codeAgence, String fseTeneur, Optional<String> codeacteur) throws IOException, InstantiationException, IllegalAccessException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        String codeact = null;
        if (tokenInfo != null) {
            System.out.println("GET : Liste des données de transaction.");
            if (codeacteur.isPresent() && !codeacteur.get().equals("0")) {
                this.setRestServerUri(this.url + INTEROP_CONTEXTE_LIVE_TRANS_PARAMETERS_ACTEUR);
                codeact = codeacteur.get();
            } else {
                this.setRestServerUri(this.url + INTEROP_CONTEXTE_LIVE_TRANS_PARAMETERS);
            }
            HashMap<String, Object> param = new HashMap();
            param.put("canal", canal);
            param.put("typeService", typeService);
            param.put("codePv", codePv);
            param.put("codeUoSrc", codeUosrc);
            param.put("codeUoDest", codeUoDest);
            param.put("montant", montant);
            param.put("valeurCompte", valeurCompte);
            param.put("codeAgence", codeAgence);
            param.put("typeCompte", typeCompte);
            param.put("teneur", fseTeneur);
            param.put("code", code);
            /* code indicatif */
            param.put("telephone", telephone);
            param.put("codeacteur", codeact);
            //    MDataParameter donnee = sendDataRequest(tokenInfo, null, MDataParameter.class);
            ResponseEntity result = this.getRessources(tokenInfo, param, "GET", Optional.empty());
            //   MDataParameter fo = new MDataParameter();
            MDataTransaction donnee = (MDataTransaction) this.convert(result.getBody(), MDataTransaction.class);
            return donnee;
        } else {
            return null;
        }
    }

    public MResponse doInterOpOperation(MOperation operation, String typeService) throws IllegalAccessException, IOException, InstantiationException {
        AuthTokenInfo tokenInfo = getTokenInfo();
        if (tokenInfo != null) {
            System.out.println("Realisation d'une operation Account Account.");
            log.debug("Realisation Paiement facture." + typeService);
            operation.setTypeService(typeService);
            this.setRestServerUri(this.url + INTEROP_CONTEXT_LIVE_OP_BASE_URL + "/operations/doSettlement");
            MResponse mResponse = sendDataRequest(tokenInfo, operation, MResponse.class);
            if (mResponse != null) {
                return mResponse;
            } else {
                return new MResponse("-1", "Impossible d'effectuer l'operation.");
            }
        } else {
            return new MResponse("-1", "Impossible d'effectuer l'operation.");
        }
    }
}
//**********************

//*****************************************************************************************************
/**
 * Service permettant de faire un virement de compte à compte
 *
 * @param operation
 * @return
 * @throws IllegalAccessException
 * @throws IOException
 * @throws InstantiationException
 */
class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return -1; // we do not want to generally read the whole stream into memory ...
    }
}
