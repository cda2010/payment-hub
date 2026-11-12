(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BackupDataDialogController', BackupDataDialogController);

        BackupDataDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'BackupData'];

    function BackupDataDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, BackupData) {
        var vm = this;

        vm.backupData = entity;
        vm.clear = clear;
        vm.save = save;
        vm.databaseTables = ['TABLE_NAME','BACKUP_DATA','ACTION_EVENT','AUDIT','AUDIT_TRAIL','BATCH_JOB_EXECUTION','BATCH_JOB_EXECUTION_CONTEXT','BENEFICIAIRE',
        'BATCH_JOB_EXECUTION_PARAMS','BATCH_STEP_EXECUTION','BATCH_STEP_EXECUTION_CONTEXT','BATCH_JOB_INSTANCE','AUTRE_PARAMETRE','AUTORISATION','BILAN_OPERATION',
        'BRANCHE_ACTIVITE','BOUTON','BILAN_TAXE','BW_ACCOUNT','BLACK_LIST','BW_CUSTOMER','CHARGEMENT_TRANSFERT','CHAMPS','CANAL','CATEGORIE_CLIENT','CLASSIFICATION_FACTURIER',
        'CANAL_TYPE_SERVICE','CANAL_COMMUNICATION','CHARGEMENT_COMPTE',
        'CLIENT',
        'CHARGEMENT_MESSAGE',
        'COMPTE_BANQUE',
        'COMPTE',
        'COMMISSION',
        'COMMERCIALE',
        'COMPTE_AUTRE',
        'COMPOSANT',
        'COMPTE_EVENT',
        'DATABASECHANGELOG',
        'DETAIL_LISTE_DIFFUSION',
        'DATABASECHANGELOGLOCK',
        'CREDENTIAL_API',
        'DETAIL_HISTORIQUE',
        'DETAIL_RELEVE_COMPTE',
        'DOCUMENT',
        'ECRAN',
        'ETIQUETTE_COMPTE',
        'EMETTEUR',
        'FACTURE',
        'FACTURE_ONEA',
        'ETIQUETTE_COMPTABLE',
        'DEVISE',
        'DETAILS_LOT',
        'EXTERNALOTP',
        'EXPORT_TABLE',
        'EVENEMENT_OPERATION',
        'HISTORIQUE',
        'GROUP_REPARTITION',
        'FAQ',
        'GROUPE_PROFIL_ACTEUR',
        'FACTURIER',
        'HIST_EXECUTION_JOB',
        'FOURNISSEUR_SERVICE',
        'FTP',
        'HISTORIQUE_TEMPLATE',
        'LIGNE_FRAIS',
        'LIEN',
        'LIGNE_MESSAGE',
        'INSTRUCTION_COMPTABLE_REGISTRE',
        'JHI_ENTITY_AUDIT_EVENT',
        'JHI_AUTHORITY',
        'LIGNE_REPARTITION_COMMISSION',
        'JHI_PERSISTENT_AUDIT_EVENT',
        'INSTRUCTION_COMPTABLE',
        'HORAIRE_AUTHENTIFICATION',
        'LIGNE_REGLEMENT',
        'LIGNE_CHARGEMENT_COMPTE',
        'LIGNE_CHARGEMENT_TRANSFERT',
        'JHI_USER',
        'JHI_PERSISTENT_AUDIT_EVT_DATA',
        'LOCALITE',
        'NOTIFICATION',
        'MOTIF_OPERATION',
        'LOG_OPERATION',
        'LOT_DETAILS',
        'OAUTH_ACCESS_TOKEN',
        'LISTE_DIFFUSION',
        'NATIONALITE',
        'LOT',
        'LISTE',
        'OAUTH_APPROVALS',
        'MACHINE',
        'MACHINE_AUDIT_EVENT',
        'PAYS',
        'PARAM_EXECUTION_JOB',
        'OAUTH_CODE',
        'OAUTH_REFRESH_TOKEN',
        'PARAMETRE_EXPORT',
        'OPERATION_SUIVIS',
        'OAUTH_CLIENT_TOKEN',
        'PAIEMENT_FACTURE_SONABEL',
        'PARAM_BACKUP_DATA',
        'PARAMETRE_APP',
        'OAUTH_CLIENT_DETAILS',
        'OPERATION_AUDIT',
        'OPERATION',
        'PERSONNE_PHYSIQUE',
        'PROFIL_ACTEUR_GROUP_REPARTITION',
        'PROFIL_ACTEUR_HORAIRE_AUTHENTIFICATION',
        'PERSONNE',
        'PRODUIT',
        'PROFIL_ACTEUR_TABLEAU_FRAIS',
        'POINT_VENTE',
        'PROFIL_ACTEUR_TABLEAU_CUMUL',
        'PROFIL',
        'PERSONNE_MORALE',
        'PIECE',
        'PROFIL_ACTEUR',
        'RECLAMATION',
        'RELEVE_COMPTE',
        'REPARTITION_OP',
        'RECEPTEUR_ALARME',
        'RECEPTEUR_EMAIL',
        'PROFIL_ROLE',
        'PROFIL_ACTEUR_TABLEAU_REPARTITION_COMMISSION',
        'REGLEMENT_TEMPLATE',
        'REPARTITION_FRAIS',
        'ROLE_VALIDATION',
        'REPARTION_FRAIS_EM_ACQ',
        'REGLEMENT',
        'REGLE_COMMISSION',
        'PTI_JOB_DETAILS',
        'SCHEDULER_JOB_INFO',
        'SERVICE_PROFIL',
        'TAXE_OPERATION_TYPE_SERVICE',
        'TEMPLATE_MESSAGE',
        'TABLEAU_CUMUL',
        'TABLEAU_FRAIS',
        'TABLEAU_REPARTITION_COMMISSION',
        'SELECT_COMPONENT_DATA',
        'SERVICE_PROFIL_TYPE_SERVICE',
        'TAUX_CHANGE',
        'TAXE_OPERATION',
        'TAXE',
        'STRUCTURE_ORGANISATION_FACTURIER','TERMINAL','TRAITEMENT_FACTURE_SONABEL','TYPE_LOCALITE','TRANSACTION_COMPTE_PTI_2021_2022','TRANSACTION_COMPTE_PTI042023',
        'TYPE_DOCUMENT','TRANSACTION_COMPTE_PTI','TYPE_RECLAMATION','TYPE_UNITE_ORGANISATION_ROLE','TYPE_UNITE_ORGANISATION','TYPE_COMPTE','TYPE_PIECE','TYPE_SERVICE','UO_DEVISE','VILLE','USER_CONTROL','USER_LISTE_BLANCHE','UTILITAIRE',
        'VERIF_OTP','UNITE_ORGANISATION','FILE_EXPORT','FILE_EXPORT_PARAM','JHI_USER_AUTHORITY','PRODUIT_CANAL','ETAPE_SERVICE','OPERATION_ETAPE','ETAPE_SERVICE_ACTION_SERVICE'];
        
       // vm.typeservices=TypeService.queryAll();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.backupData.id !== null) {
                BackupData.update(vm.backupData, onSaveSuccess, onSaveError);
            } else {
                BackupData.save(vm.backupData, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:backupDataUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
