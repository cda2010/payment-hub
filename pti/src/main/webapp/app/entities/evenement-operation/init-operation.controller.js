(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InitOperationController',InitOperationController);

    InitOperationController.$inject = ['$http', '$uibModalInstance', '$uibModal', '$state', '$rootScope', 'ChoisirUo', 'BrancheActivite', 'MotifOperation', 'isMaj', 'Notification', 'isExpired','isChange' ,'BASE_URL', 'Utils', 'CancelAlert', 'OperationUtils', 'DateUtils', 'isViewing', 'RequestUtils', 'TypePiece', 'InitSteps', 'Nationalite', '$timeout', '$scope', '$stateParams', 'entity'];

    function InitOperationController($http, $uibModalInstance, $uibModal, $state, $rootScope, ChoisirUo, BrancheActivite, MotifOperation, isMaj, Notification, isExpired, isChange,BASE_URL, Utils, CancelAlert, OperationUtils, DateUtils, isViewing, RequestUtils, TypePiece, InitSteps, Nationalite, $timeout, $scope, $stateParams, entity) {
        var vm = this;
        var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
        var URL = BASE_URL + "api/reporting/operations/bordereau";
        vm.operation = entity;
        vm.isExpired = isExpired;
        vm.isChange=isChange;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.motifs = [];
        vm.branchesActivites = BrancheActivite.query();
        vm.nationalites = Nationalite.query();
        vm.typepieces = TypePiece.query();
        // vm.devises=Devise.query();
        vm.verif = false;
        vm.pieceB = null;
        vm.isFraisDispo = false;
        vm.message = null;
        vm.isViewing = isViewing;
        vm.typeService = "ENVOI_CASH";
        vm.isMaj = isMaj;
        vm.taxes = [];
        vm.simulateur = function () {
            console.log('simulateur de frais');
            $state.go('simulation-operation');
        };

        if (!vm.isViewing) {
            vm.montantEnvoye = 0;
            vm.operation.montantTaxe = 0;
            vm.operation.montantOpEnDevise=0;
            vm.operation.uoAcquereurId = null;
        }
        vm.defaultDevise = {
            deviseId: null
        };
        vm.deviseP = {
            deviseLibelle: "",
            deviseId: null
        };
        vm.devises = [];
        vm.tauxChange = {
            taux: 1
        };




        /**
         Recherche du tableau de change
         */

        vm.itemSearch = {
            "organisationId": null,
            "etiquette": null,
            "categorieCompte": null
        };

        vm.choisirUo = function () {
            vm.operation.lieuPaiementId = null;
            vm.organisation = null;

            RequestUtils.GetUoFilsByType.get({
                type: "FSE",
                uoId: vm.operation.uoAcquereurId
            }, function (datas) {
                var filiale = _.find(datas, {
                    'estFiliale': true
                });
                var data = {
                    "paysId": vm.operation.uoAcquereurId,
                    "fseId": filiale.id,
                    "acteurs": ["AGENCE", "MASTER", "SOUS_DISTRIBUTEUR"]
                };
                ChoisirUo.open($scope, data);
            });

        };


        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.operation.lieuPaiementId = result.id;
            }
        });


        vm.personneE = {
            typePersonne: 'PHYSIQUE'
        };
        vm.personneB = {
            typePersonne: 'PHYSIQUE'
        };
        OperationUtils.initVerificationOrganisatioType(vm);

        function clear() {
            vm.isSaving = false;
            $uibModalInstance.close(true);
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else
                $state.reload("evenement-operation");
        }


        function save() {
            CancelAlert.confirm(function () {
                vm.isSaving = true;
                console.log("enregistrement de l'operation");
                $http.jsonp(url)
                    .success(function (data) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function (datas) {
                            var datas = {};
                            var beneficiaire = {};
                            var emetteur = {};
                            beneficiaire=null;
                            emetteur.personneDTO = vm.personneE;
                            emetteur.personnePhysiqueDTO = vm.personnePhysiqueE;
                            emetteur.pieceDTO = vm.pieceE;
                            vm.operation.canalLibelleCanal = "WEB";
                            vm.operation.taxes = vm.taxes;
                            datas.operationDTO = vm.operation;
                            datas.emetteur = emetteur;
                            datas.beneficiaire=beneficiaire;
                            datas.typeService = vm.typeService;
                            console.log('datas',datas);
                            RequestUtils.SaveEvenementClient.save(datas, function (result) {
                                if (result != null) {
                                    console.log('result',result);
                                    onSaveSuccess(result);

                                }
                            }, function () {
                                vm.isSaving = false;
                            });
                        }, function () {
                            vm.isSaving = false;
                        });
                    }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });

        }

        function onSaveSuccess(result) {
            Notification.success("Opération effectué!");
            vm.isSaving = false;
            $uibModalInstance.close(true);
            $state.reload("evenement-operation");
        }

        function onSaveError() {
            console.log("error result");
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.steps = [
            'Expéditeur'
        ];
        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }
        var datas = {
            vm: vm,
            steps: vm.steps
        };
        InitSteps.initSteps(datas);
        OperationUtils.init($scope, vm, vm.typeService, $uibModalInstance);

        vm.goToStepNotVerif = function (index) {
            if (!angular.isUndefined(vm.steps[index])) {
                vm.selection = vm.steps[index];
            }
        };

        vm.sendCodeRetrait = function () {
            CancelAlert.confirm(function () {
                RequestUtils.OperationSendCodeRetrait.save(vm.operation, function () {
                    Notification.success("Code retrait renvoyé !");
                    $uibModalInstance.close(true);
                }, function () {
                    vm.isSaving = false;
                });
            });
        };

        vm.changeStatutOp = function() {
            console.log('-----Start change---------');
            RequestUtils.ChangeOperationStatut.get({
                operationId: vm.operation.id
            }, function (datas) {
                if(datas!=null) {
                    console.log(datas);
                    $uibModalInstance.close(true);
                    Notification.success("Statut de l'opération changé avec succès!");
                    $state.go('operation');
                }

            })
        };

        vm.annulationOperationAvecFrais = function () {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationEnvoiCashAvecRetourFrais.get({
                    operationId: vm.operation.id
                }, function (datas) {
                    if (datas != null) {
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!")
                    }
                });
            });
        };


        vm.annulationOperationSansFrais = function () {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationEnvoiCashSansRetourFrais.get({
                    operationId: vm.operation.id
                }, function (datas) {
                    if (datas != null) {
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!")
                    }
                });
            });
        };

        /**
         * Mise à jour d'une operation
         */
        vm.majOperation = function (operation) {
            CancelAlert.confirm(function () {
                console.log("Mise à jour de l'operation");
                RequestUtils.OperaionMaj.update(operation, function (operation) {
                    Notification.success("Mise à jour effectuée!");
                    $uibModalInstance.close(true);
                });
            });
        };


        function confirm() {
            CancelAlert.confirm(function () {
                vm.isSaving=true;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";

                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {
                            vm.operation.login=vm.username;
                            vm.operation.motif=vm.operation.commentaire;
                            console.log("Validation",vm.operation);
                            RequestUtils.EvenementTraitement.save(vm.operation, function(){
                                console.log("Operation validée! ");
                                $uibModalInstance.close(true);
                                $state.reload('evenement-operation' );
                            });
                        });
                    }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        }

    }
})();
