(function () {
    'use strict';
    angular
            .module('ptiApp')
            .factory('RequestUtils', RequestUtils);
    RequestUtils.$inject = ['$resource', 'BASE_URL_API'];

    function RequestUtils($resource, BASE_URL_API) {
        const FIND_PERSONNE = BASE_URL_API + 'personnes/recherche/telephone';
        const FIND_TABLEAU_REPARTITION_COMMISSION_BY_CRITERE = BASE_URL_API + 'tableau-repartition-commissions/search';
        const CALCUL_FRAIS = BASE_URL_API + 'operations/calcul-frais';
        const CALCUL_FRAIS_SIMULATION = BASE_URL_API + 'operations/simulation-calcul-frais';
        const COMPTE_WHERECOMPTE_PRINCIPAL_IS_NULL_ = BASE_URL_API + 'comptes/comptePrincipaleIsNull';
        const COMPTE_AUTRE_BY_ETIQUETTE = BASE_URL_API + 'compte-autres/by-etiquette';

        const COMPTE_AUTRE_BY_ID = BASE_URL_API + 'compte-autres/:id';
        const FIND_GROUPREPARTITIONS = BASE_URL_API + 'group-repartitions/all';


        const TRANSACTION_BY_COMPTE_AUTRE = BASE_URL_API + 'transaction-compte-ptis/ByCompteAutre';
        const LISTE_ETIQUETTE_COMPTE = BASE_URL_API + 'etiquette-comptes_list';
        const LISTE_TYPE_SERVICE = BASE_URL_API + 'type-services_list';
        const OPERATION_ENVOIE_CASH = BASE_URL_API + "operations/envoie-cash";
        const OPERATION_VERIF_CUMUL = BASE_URL_API + "operations/verification-cumul";
        const OPERATION_MAD = BASE_URL_API + "operations/mad";
        const OPERATION_ATTENTE = BASE_URL_API + "operationsEnAttente";
        const OPERATION_RETRAIT_CASH = BASE_URL_API + "operations/retrait-cash";
        const OPERATION_APPROVISIONNEMENT_COMPTE = BASE_URL_API + "operations/approvisionnement-compte";
        const OPERATION_REGLEMENT_COMPTE = BASE_URL_API + "operations/valider-reglement-compte";
        const OPERATION_CREATION_MONNAIE = BASE_URL_API + "operations/creation-monnaie";

        const OPERATION_REGLEMENT = BASE_URL_API + "operations/reglement-compte";
        const OPERATION_REGLEMENT_TEMPLATE = BASE_URL_API + "reglements/reglementTemplate";


        const OPERATION_RETRAIT_COMPTE = BASE_URL_API + "operations/retrait-compte";
        const OPERATION_ANNULATION_APPROVISIONNEMENT = BASE_URL_API + "operations/annulation-retrait-cash";
        const ORGANISATION_BY_TYPE = BASE_URL_API + "unite-organisations/:type/typeUO";
        const ORGANISATION_BY_TYPE_OPTIMISE = BASE_URL_API + "unite-organisations/:type/typeUO-optimise";
        const LISTE_CLIENT_OPTIMISE = BASE_URL_API + "clients/type-optimise";
        const ORGANISATION_FILS_TYPE = BASE_URL_API + "unite-organisations/uo_fils_by_type";
        const ORGANISATION_FILS_TYPE_OPTMISER = BASE_URL_API + "unite-organisations/uo_fils_by_type_optimiser";
        const ORGANISATION_BY_TYPE_OPTIMISER = BASE_URL_API + "unite-organisations/findByTypeUo-optimiser";
        const ORGANISATION_FILS_BY_UO = BASE_URL_API + "unite-organisations/uosFils-by-uo";
        const ORGANISATION_PERE_BY_TYPEUO = BASE_URL_API + "unite-organisations/pere-by-type";
        const OPERATION_VERIFICATION = BASE_URL_API + "operations/verification-avant-retrait";
        const OMNICANAL_GET_FSE = BASE_URL_API + "omnnicanal/fournisseur";
        const OMNICANAL_GET_FSE_RELATION = BASE_URL_API + "omnnicanal/listRelation";
        const GET_3_TYPE_OF_SERVICE = BASE_URL_API + "type-services/someService";
        const GET_CODE_POINT_VENTE = BASE_URL_API + "point-ventes/codePV";
        const OMNICANAL_GET_VERIFICATION_VERS = BASE_URL_API + "omnicanal/verif-operation";
        const PROFIL_CREATED_BY_USER_CONNECTED = BASE_URL_API + "profils/byUserCreated";
        const ROLE_BY_TYPE_UO = BASE_URL_API + "type-unite-organisations/:id/roles";
        const PROFIL_BY_TYPEUO = BASE_URL_API + "profilsByTypeUO/:typeId";
        const OMNICANAL_TEST = BASE_URL_API + "omnicanal/test";
        const OMNICANAL_INFOS_CLIENT = BASE_URL_API + "omnnicanal/infoClient";
        const OMNICANAL_SEND_SMS = BASE_URL_API + "omnnicanal/sendSms";
        const USERS_ORGANISATION = BASE_URL_API + "users/unite-organisation";
        const USERS_ORGANISATION_WITHOUT_PAGE = BASE_URL_API + "users/unite-organisation-without-page";
        const POINT_VENTE_USERS = BASE_URL_API + "point-ventes/users";
        const OPERATION_TRAITEMENT = BASE_URL_API + "operations/traitement-operation";
        const EVENEMENT_TRAITEMENT = BASE_URL_API + "operations/traitement-operation-evenement";
        const USER_ORGANISATION = BASE_URL_API + "unite-organisations/user-organisation";
        const BILAN_OPERATION = BASE_URL_API + "operations/somme_montant";
        const BILAN_OPERATION_OPTIMISE = BASE_URL_API + "operations/somme_montant_optimise";
        const BILAN_MENSUEL = BASE_URL_API + "operations/bilan_mensuel";
        const BILAN_COUNTRY = BASE_URL_API + "operations/bilan_operations_agence";
        const SEND_CODE_RETRAIT = BASE_URL_API + "operations/send_code_retrait";
        const ANNULATION_ENVOI_CASH_AVEC_RETOUR_FRAIS = BASE_URL_API + "operations/annulation_envoi_cash_avec_retour_frais";
        const ANNULATION_ENVOI_CASH_SANS_RETOUR_FRAIS = BASE_URL_API + "operations/annulation_envoi_cash_sans_retour_frais";
        const ANNULATION_ACCOUNT_AVEC_RETOUR_FRAIS = BASE_URL_API + "operations/annulation_account_avec_retour_frais";
        const ANNULATION_OPERATION = BASE_URL_API + "operations/annulation_operation";
        const OPERATION_COMPTABILSATION = BASE_URL_API + "operations/comptabilisation-op";
        const VERIF_OPERATION_EXPIRE = BASE_URL_API + "operations/verif-expiration";
        const SEARCH_OPERATION_CODE = BASE_URL_API + "operations/search_codeOperation";
        const MAJ_OPERATION = BASE_URL_API + "operations/mise_jour_statut";
        const ANNULATION_APPEL_FOND = BASE_URL_API + "operations/annulation_appel_fond";
        const UPDATE_USER_NBRFAILURE = BASE_URL_API + "users/update_nbr_failure";
        const PARAMETREAPP_SEARCH = BASE_URL_API + "parametre-apps/search";
        const CALCUL_TAXE = BASE_URL_API + "taxe-operations/findByTaxe";
        const GET_DEVISE = BASE_URL_API + "uo-devises/byOrganisation";
        const GET_DEVISE_DEFAULT_SRC = BASE_URL_API + "uo-devises/defaultSrc";
        const GET_DEVISE_DEFAULT_ORG = BASE_URL_API + "uo-devises/defaultByOrg";
        const GET_TAUX_CHANGE = BASE_URL_API + "taux-changes/byCriteria";
        const ORG_FILS = BASE_URL_API + "unite-organisations/uo_fils_by_ville";
        const OMNICANAL_GET_EVENEMENT = BASE_URL_API + "operations/list_evenement";
        const AUTORISATION_BY_CLIENT = BASE_URL_API + "autorisations/byClient";
        const SERVICES_CLIENT = BASE_URL_API + "type-services/clientServices";
        const SERVICES_FRAIS = BASE_URL_API + "type-services/fraisServices";
        const INFOS_CLIENT = BASE_URL_API + "clients/byTelephone";
        const OPERATION_REDR = BASE_URL_API + "operations/redressement";
        const OPERATION_BY_PERSONNE = BASE_URL_API + "operations/search_by_personne";
        const OPERATION_OPTIMISE = BASE_URL_API + "operations-optimise";
        const COMPTE_TITULAIRE = BASE_URL_API + "comptes/titulaire";
        const VERIF_TOKEN = BASE_URL_API + "machines/verif_token";
        const PIECE_TITULAIRE = BASE_URL_API + "piecesOfTitulaireId/:id";
        const CLIENT_INFOS = BASE_URL_API + "clients/personne_search";
        const CLIENT_INFOS_OPTIMISE = BASE_URL_API + "clients/personne_search_optimise";
        const CHANGE_Op_Statut = BASE_URL_API + "operation/change-statut-operation";
        const DOUBLONSAVEURL = BASE_URL_API + "saveDoublon";
        const SAVE_COMPTE_BANQUE = BASE_URL_API + "saveCompteBanque-of-client";
        const SAVE_EVENEMENT_CLIENT = BASE_URL_API + "operations/save-evenement-client";
        const ORGANISATION_PAGINER_OPTIMISE = BASE_URL_API + "unite-organisations-paginer-optimise";
        const USER_OPTIMISE = BASE_URL_API + "users_optimise";
        const ORGANISATION_PERE_BY_TYPEUO_OPTIMISE = BASE_URL_API + "unite-organisations/pere-by-type-optimise";
        const PROFIL_BY_TYPEUO_OPTIMISE = BASE_URL_API + "profilsByTypeUO-optimise/:typeId";
        const AUTHORITIES_BY_PROFIL = BASE_URL_API + "profilsAndRolesByTypeUO/:typeId";
        const COMMISSION_OPTIMISE = BASE_URL_API + "commissions-optimise";
        const PROFIL_OPTIMISE = BASE_URL_API + "profils_optimise";
        const LIGNE_REPARTITION_COMMISSION = BASE_URL_API + "ligne-repartition-commissions/tableauRepartition/byId-optimise";
        const LIST_MACHINE_OPTIMISE = BASE_URL_API + "machines-optimise";
        const TYPE_UNITE_ORGANISATION = BASE_URL_API + "type-unite-organisations-optimise";
            const COMPTE_BANQUE_EXISTE= BASE_URL_API + "/compte/compte-banque-verif";
        const LISTE_TERMINAL_FILTRE = BASE_URL_API + "/terminals/filtre"
        const INFOSUPP = BASE_URL_API + "/infosupp/libelle" ;
        const TYPEUOBYUI = BASE_URL_API + "type-unite-organisations-optimise/type";
        const LIGNE_FICHIER_DETAIL= BASE_URL_API + "ligne-fichier/organisation";
        return {
            Print: $resource(BASE_URL_API + "reporting/operations/bordereau", {}, {
                'save': {
                    method: 'POST',
                    isArray: false,
                    transformResponse: function (data) {
                        return {
                            url: data
                        };
                    }
                }
            }),
            GetAllGroupRepartitions: $resource(BASE_URL_API + 'group-repartitions/all', {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            GetAllUsers: $resource(BASE_URL_API + "users/all_users", {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), BilanCompteAutre: $resource(BASE_URL_API + "transaction-compte-ptis/bilanTransaction", {}, {
                'get': {
                    method: 'GET',
                    isArray: false,
                    transformResponse: function (data) {
                        return {
                            montant: data
                        };
                    }
                }
            }), ExportCsv: $resource(BASE_URL_API + "operations/download", {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }), ExportCommissionCsv: $resource(BASE_URL_API + "commissions/download", {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }), ExportClientCsv: $resource(BASE_URL_API + "listeClient/CSV", {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }), ClientInfos: $resource(CLIENT_INFOS, {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }),
            ClientInfosOptimise: $resource(CLIENT_INFOS_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }),
            GetPiecesOfTitulaire: $resource(PIECE_TITULAIRE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            VerifToken: $resource(VERIF_TOKEN, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            CompteTitulaire: $resource(COMPTE_TITULAIRE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            OperationOptimise: $resource(OPERATION_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            OperationByPersonne: $resource(OPERATION_BY_PERSONNE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            CommissionOptimise: $resource(COMMISSION_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            ProfilOptimise: $resource(PROFIL_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            //optimisation des utilisateurs
            UserOptimise: $resource(USER_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            doOperationRedressement: $resource(OPERATION_REDR, {}, {
                'save': {
                    method: 'PUT',
                    isArray: false
                }
            }), HoraireAuth: $resource(BASE_URL_API + "/horaire-authentifications/all", {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), TableauF: $resource(BASE_URL_API + "/tableau-frais/all", {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), TableauR: $resource(BASE_URL_API + "/tableau-repartition-commissions/all", {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), TableauC: $resource(BASE_URL_API + "/tableau-cumuls/all", {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), ServicesClient: $resource(SERVICES_CLIENT, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), //Liste des services clients
            ServicesFrais: $resource(SERVICES_FRAIS, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), //Liste des autorisation d'un client ayant effectue une souscription
            AutorisationByClient: $resource(AUTORISATION_BY_CLIENT, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), ListEvenement: $resource(OMNICANAL_GET_EVENEMENT, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), OrgFils: $resource(ORG_FILS, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), /* Estimation du montant de l'operation à travers le ttc*/
            SimulationCalculFrais: $resource(CALCUL_FRAIS_SIMULATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), getDefaultDeviseSrc: $resource(GET_DEVISE_DEFAULT_SRC, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), getDefaultDeviseOrg: $resource(GET_DEVISE_DEFAULT_ORG, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), getTauxChange: $resource(GET_TAUX_CHANGE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), getDevises: $resource(GET_DEVISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }), getTaxes: $resource(CALCUL_TAXE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }), getParametreApp: $resource(PARAMETREAPP_SEARCH, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), UpdateNbrFailure: $resource(UPDATE_USER_NBRFAILURE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), /* recherche d'une operation en fonction du code de l'operation*/
            AnnulationAppelFond: $resource(ANNULATION_APPEL_FOND, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), /* recherche d'une operation en fonction du code de l'operation*/
            OperaionMaj: $resource(MAJ_OPERATION, {}, {
                'update': {
                    method: 'PUT',
                    isArray: false
                }
            }),
            /* recherche d'une operation en fonction du code de l'operation*/
            OperationSearch: $resource(SEARCH_OPERATION_CODE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /* verification de l'existence d'une operation expiree*/
            VerificationOperationExpiree: $resource(VERIF_OPERATION_EXPIRE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /*Annulation d'une operation*/
            ComptabilisationOp: $resource(OPERATION_COMPTABILSATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), AnnulationEnvoiCashAvecRetourFrais: $resource(ANNULATION_ENVOI_CASH_AVEC_RETOUR_FRAIS, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), AnnulationEnvoiCashSansRetourFrais: $resource(ANNULATION_ENVOI_CASH_SANS_RETOUR_FRAIS, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), AnnulationAccountAvecRetourFrais: $resource(ANNULATION_ACCOUNT_AVEC_RETOUR_FRAIS, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            AnnulationOperation: $resource(ANNULATION_OPERATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            OrganisationsFilsByType: $resource(ORGANISATION_FILS_TYPE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            OrganisationsFilsByTypeOptimiser: $resource(ORGANISATION_FILS_TYPE_OPTMISER, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            ChangeOperationStatut: $resource(CHANGE_Op_Statut, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }

            }),
            /* Operation MAD*/
            OperationSendCodeRetrait: $resource(SEND_CODE_RETRAIT, {}, {
                'save': {
                    method: 'PUT',
                    isArray: false
                }
            }),
            PointVenteCode: $resource(GET_CODE_POINT_VENTE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            BilanOpByCountryAndOp: $resource(BILAN_COUNTRY, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), BilanMensuel: $resource(BILAN_MENSUEL, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            BilanOp: $resource(BILAN_OPERATION_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            UserOrganisation: $resource(USER_ORGANISATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), /* Operation MAD*/
            OperationTraitement: $resource(OPERATION_TRAITEMENT, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }), /* Operation MAD*/
            EvenementTraitement: $resource(EVENEMENT_TRAITEMENT, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            OperationMad: $resource(OPERATION_MAD, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            SomeTypeService: $resource(GET_3_TYPE_OF_SERVICE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            TableauRepartitionCommissionByCritere: $resource(FIND_TABLEAU_REPARTITION_COMMISSION_BY_CRITERE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            EtiquetteCompteList: $resource(LISTE_ETIQUETTE_COMPTE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            TypeServiceList: $resource(LISTE_TYPE_SERVICE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            TransationByCompteAutre: $resource(TRANSACTION_BY_COMPTE_AUTRE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            CompteAutreByEtiquette: $resource(COMPTE_AUTRE_BY_ETIQUETTE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            CompteAutreByID: $resource(COMPTE_AUTRE_BY_ID, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            CompteWherePrincipalIsNull: $resource(COMPTE_WHERECOMPTE_PRINCIPAL_IS_NULL_, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            OperationAttente: $resource(OPERATION_ATTENTE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            InfoClientSouscri: $resource(INFOS_CLIENT, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), InfoClient: $resource(OMNICANAL_INFOS_CLIENT, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }), SendSms: $resource(OMNICANAL_SEND_SMS, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /*Mise à jour des utilisateurs d'un point de vente*/
            PointVenteUsers: $resource(POINT_VENTE_USERS, {}, {
                'save': {
                    method: 'PUT',
                    isArray: false
                }
            }),
            OrganisationUsers: $resource(USERS_ORGANISATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            OrganisationUsersWithoutPage: $resource(USERS_ORGANISATION_WITHOUT_PAGE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            })
            ,
            TestOmnicanal: $resource(OMNICANAL_TEST, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            VerificationVersement: $resource(OMNICANAL_GET_VERIFICATION_VERS, {}, {
                'query': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /*Les profils Par type unite Organisation*/
            ProfilsByTypeUO: $resource(PROFIL_BY_TYPEUO, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            ProfilsByTypeUOOptimise: $resource(PROFIL_BY_TYPEUO_OPTIMISE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            ProfilsAndRolesByTypeUOOptimise: $resource(AUTHORITIES_BY_PROFIL, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            /*Les roles en fonction du type d'organisation*/
            GetRolesOfTypeUniteOrganisation: $resource(ROLE_BY_TYPE_UO, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            /*Les profils crées par l'utilisateur connecte*/
            GetProfilsCreatedByUserConnected: $resource(PROFIL_CREATED_BY_USER_CONNECTED, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), GetFournisseur: $resource(OMNICANAL_GET_FSE, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }), GetFournisseurRelation: $resource(OMNICANAL_GET_FSE_RELATION, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),
            /* Annulation   d'operation d'approvisionnement de compte*/
            OperationAnnulationRetraitCash: $resource(OPERATION_ANNULATION_APPROVISIONNEMENT, {}, {
                'update': {
                    method: 'PUT',
                    isArray: false
                }
            }),
            /* Operation d'approvisionnement  de compte*/
            OperationApprovisionnementCompte: $resource(OPERATION_APPROVISIONNEMENT_COMPTE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),

            /* Operation reglement valider  */
            OperationReglementCompte: $resource(OPERATION_REGLEMENT_COMPTE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),

            CreationMonnaieCompte: $resource(OPERATION_CREATION_MONNAIE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            ReglementCompte: $resource(OPERATION_REGLEMENT, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            ReglementTemplate: $resource(OPERATION_REGLEMENT_TEMPLATE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            /* Operation d'envoie de cash*/
            OperationRetraitCash: $resource(OPERATION_RETRAIT_CASH, {}, {
                'save': {
                    method: 'PUT',
                    isArray: false
                }
            }), //Operation retrait Compte
            OperationRetraitCompte: $resource(OPERATION_RETRAIT_COMPTE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            /* Lister les UO fils par type d'une unite d'organisation */
            GetUoFilsByType: $resource(ORGANISATION_FILS_BY_UO, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            /* Récupération de la liste des organisation en fonction du type uo optimiser*/
            GetUoByTypeOptimiser: $resource(ORGANISATION_BY_TYPE_OPTIMISER, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            /*Récupération de la liste des organisation en fonction du type uo optimiser Dashboard */
            GetOrganisationByTypeOptimise: $resource(ORGANISATION_BY_TYPE_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),

            LigneRepartitionCommissionOptimise: $resource(LIGNE_REPARTITION_COMMISSION, {}, {
                'query': {
                    method: 'GET',
                    isArray: true
                }
            }),

            /* retrouver un UO Pere d'un typeUO donné */
            GetUoPereByType: $resource(ORGANISATION_PERE_BY_TYPEUO, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),

            GetUoPereByTypeOptimise: $resource(ORGANISATION_PERE_BY_TYPEUO_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),

            ListUiniteOrganisationOptimise: $resource(TYPE_UNITE_ORGANISATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),

            /* verification de l'existence d'une operation*/
            VerificationOperation: $resource(OPERATION_VERIFICATION, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /* Calcul du frais d'une transaction de type Envoie Cash*/
            CalculFrais: $resource(CALCUL_FRAIS, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
            /* Recherche d'une personne en fonction du telphone et du docIdentification(typePiece,referencePiece)*/
            FindPersonne: $resource(FIND_PERSONNE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            /* Operation d'envoie de cash*/
            OperationEnvoieCash: $resource(OPERATION_ENVOIE_CASH, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }), /* Operation verification */
            OperationVerifCumul: $resource(OPERATION_VERIF_CUMUL, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            /* Recherche des organisation en fonction du type */
            GetOrganisationByType: $resource(ORGANISATION_BY_TYPE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            GetListClientOptimise: $resource(LISTE_CLIENT_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),

            GetListMachineOptimise: $resource(LIST_MACHINE_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),

            /* Save doublon*/
            SaveDoublon: $resource(DOUBLONSAVEURL, {}, {
                'save': {
                    method: 'POST',
                    isArray: true
                }
            }),
            SaveCompteBanque: $resource(SAVE_COMPTE_BANQUE, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            SaveEvenementClient: $resource(SAVE_EVENEMENT_CLIENT, {}, {
                'save': {
                    method: 'POST',
                    isArray: false
                }
            }),
            /*Service de récupération des organisations en mode native query*/
            GetOrganisationPaginerOptimise: $resource(ORGANISATION_PAGINER_OPTIMISE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
           VerifCompteBanqueExist: $resource(COMPTE_BANQUE_EXISTE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            GetTerminalOptimiser: $resource(LISTE_TERMINAL_FILTRE, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            getInfoLibelle: $resource(INFOSUPP, {}, {
                'get': {
                    method: 'GET',
                    isArray: true
                }
            }),
            getTypeUoByUoId: $resource(TYPEUOBYUI, {}, {
                'get': {
                    method: 'GET',
                    isArray: false
                }
            }),
             GetLigneFichierDetailByOrganisation: $resource(LIGNE_FICHIER_DETAIL, {}, {
                'get' : {
                    method: 'GET',
                    isArray: true
                }
            })
        };
    }
})();
