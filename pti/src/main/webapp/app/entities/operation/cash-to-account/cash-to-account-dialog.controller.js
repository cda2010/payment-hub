(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CashToAccountDialogController', CashToAccountDialogController);

    CashToAccountDialogController.$inject = ['$http', '$sessionStorage', 'isMaj', '$uibModalInstance', '$state', 'BrancheActivite', 'MotifOperation', 'BASE_URL', 'Utils', 'Notification', 'CancelAlert', 'DataUtils', 'OperationUtils', 'Devise', 'DateUtils', 'isViewing', 'RequestUtils', 'TypePiece', 'InitSteps', 'Nationalite', '$timeout', '$scope', 'entity'];

    function CashToAccountDialogController($http, $sessionStorage, isMaj, $uibModalInstance, $state, BrancheActivite, MotifOperation, BASE_URL, Utils, Notification, CancelAlert, DataUtils, OperationUtils, Devise, DateUtils, isViewing, RequestUtils, TypePiece, InitSteps, Nationalite, $timeout, $scope, entity) {

        var vm = this;
        vm.save = save;
        vm.operation = entity;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.typeService = "CASH_ACCOUNT";
        vm.motifs = MotifOperation.query();
        vm.branchesActivites = BrancheActivite.query();
        vm.nationalites = Nationalite.query();
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.isViewing = isViewing;
        vm.taxes = [];
        vm.totalTaxes = 0;
        vm.montantEnvoye = 0;
        vm.defaultDeviseB = null;
        vm.isMaj = isMaj;
        vm.fseSrc = {
            id: null
        };
        vm.fseDest = {
            id: null
        };
        vm.token = null;
        vm.clear = function clear() {
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else {
                $state.reload("operation-envoi-cash-to-account");
            }
        };


        vm.taxes = [];
        vm.montantEnvoye = 0;
        vm.operation.montantOpEnDevise;
        vm.operation.montantTaxe = 0;
        vm.deviseP = {
            deviseLibelle: ""
        };
        vm.typeBeneficiaire = "Autre";
        vm.isSaving = false;

        vm.personneE = {
            typePersonne: 'PHYSIQUE'
        };
        vm.personneB = {
            typePersonne: 'PHYSIQUE'
        };
        vm.compte = {
            estCompteBanque: null,
            estCompteAutre: null
        };
        vm.oldUoAcquereurId;
        vm.oldUoEmetteurId;
        OperationUtils.init($scope, vm, vm.typeService, $uibModalInstance);
        OperationUtils.initVerificationOrganisatioType(vm);

        /**
         * Enregistrement des informations d l'opération
         */
        function save() {
            vm.isSaving = true;
            CancelAlert.confirm(function () {
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function (datas) {
                            console.log("enregistrement de l'operation");
                            var datas = {};
                            var beneficiaire = {};
                            var emetteur = {};
                            beneficiaire.personneDTO = vm.personneB;
                            beneficiaire.personnePhysiqueDTO = vm.personnePhysiqueB;
                            beneficiaire.personneMoraleDTO = vm.personneMoraleB;
                            beneficiaire.pieceDTO = vm.pieceB;
                            if (vm.typeService == "CASH_ACCOUNT") {
                                if (vm.estCompteBanqueB == "OUI") {
                                    beneficiaire.compteCBIDTO = vm.comptesB;
                                    //beneficiaire.compteCBIDTO.type="Banque";
                                } else {
                                    beneficiaire.compteCBIDTO = {}
                                    beneficiaire.compteCBIDTO.numeroCompte = vm.refCompteB;
                                    beneficiaire.compteCBIDTO.type = "Autre";
                                    beneficiaire.compteCBIDTO.codeFse = vm.fseDest;
                                }
                            }
                            emetteur.personneDTO = vm.personneE;
                            emetteur.personnePhysiqueDTO = vm.personnePhysiqueE;
                            emetteur.personneMoraleDTO = vm.personneMoraleE;
                            emetteur.pieceDTO = vm.pieceE;

                            vm.operation.canalLibelleCanal = "WEB";
                            // uoEm: fse de l'organisation de user connecté 
                            // uoAcq: fse du compte dest
                            vm.oldUoEmetteurId = vm.operation.uoEmetteurId;
                            vm.oldUoAcquereurId = vm.operation.uoAcquereurId;
                            vm.operation.uoEmetteurId = vm.userOrganisation.fournisseurId;
                            vm.operation.uoAcquereurId = vm.fseDest;

                            datas.operationDTO = vm.operation;
                            datas.beneficiaire = beneficiaire;
                            datas.emetteur = emetteur;
                            datas.typeService = vm.typeService;
                            if (vm.deviseP != null)
                                vm.operation.devisePaiementId = vm.deviseP.deviseId;
                            RequestUtils.OperationVerifCumul.save(datas, function (result) {
                                if (result != null) {
                                    switch (result.action) {
                                        case "BLOQUE":
                                            Notification.error(result.message + "!");
                                            vm.isSaving = false;
                                            break;
                                        case "NON_BLOQUANT":
                                            CancelAlert.confirm(function () {
                                                datas.operationDTO.statut = "ENATTENTE_VALIDATION";
                                                saveNow(datas);
                                            }, function () {
                                                vm.isSaving = false;
                                            }, result.message + "\n Soumettre pour validation?");
                                            break;
                                        default:
                                            saveNow(datas);
                                            break;
                                    }
                                } else {
                                    saveNow(datas);
                                }
                            }, function () {
                                resetValues();
                            });
                        }, function () {
                            vm.isSaving = false;
                        });

                    }).error(function () {
                        Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                    });
            });
        }

        function resetValues() {
            console.log("reset ...");
            vm.operation.uoEmetteurId = vm.oldUoEmetteurId;
            vm.operation.uoAcquereurId = vm.oldUoAcquereurId;
            vm.isSaving = false;
        }

        function saveNow(datas) {
            RequestUtils.OperationMad.save(datas, function (result) {
                $state.reload("operation-envoi-cash-to-account");
                vm.isSaving = false;
                Notification.success("Opération effectuée avec succès!");
                Utils.postPrintUrl(vm.url, {
                    'operationId': result.id
                });
            }, function () {
                resetValues();
            });
        }

        vm.annulationOperationAvecFrais = function (avecFrais) {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationAccountAvecRetourFrais.get({
                    operationId: vm.operation.id,
                    avecFrais: avecFrais
                }, function (datas) {
                    if (datas != null) {
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!")
                    }
                });
            });
        };



        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.steps = [
            'Information',
            'Expéditeur',
            'Bénéficiaire'
        ];
        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }
        var datas = {
            vm: vm,
            steps: vm.steps
        };
        vm.estCompteBanque = true;
        InitSteps.initSteps(datas);

        $scope.$watch('vm.estCompteBanque', function (boolean) {
            if (angular.isDefined(boolean)) {
                if (vm.estCompteBanque == "OUI") {
                    console.log("est compte banque");
                    vm.compte.estCompteBanque = true;
                    vm.compte.estCompteAutre = null;

                } else {
                    console.log("est compte autre");
                    vm.compte.estCompteAutre = true;
                    vm.compte.estCompteBanque = null;

                }
            }
        });
    }
})();