(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AccountCashController', AccountCashController);

    AccountCashController.$inject = ['$http', '$sessionStorage', 'ChoisirUo', '$uibModalInstance', 'isMaj', 'BASE_URL', 'Utils', 'CancelAlert', '$uibModal', 'isViewing','estAnnuler', 'DataUtils', 'typeService', 'DateUtils', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', '$scope', '$rootScope', 'entity', '$state'];

    function AccountCashController($http, $sessionStorage, ChoisirUo, $uibModalInstance, isMaj, BASE_URL, Utils, CancelAlert, $uibModal, isViewing,estAnnuler, DataUtils, typeService, DateUtils, OperationUtils, Notification, RequestUtils, InitSteps, $scope, $rootScope, entity, $state) {
        var vm = this;
        vm.save = save;
        vm.doAnnulation=doAnnulation;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.operation = entity;
        vm.typeService = typeService;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.isViewing = isViewing;
        vm.estCompteBanque = "OUI";
        vm.estCompteBanqueB = "OUI";
        vm.isMaj = isMaj;
        vm.estAnnuler=estAnnuler;
        vm.motifs = [];


        vm.comptesB = {};

        vm.comptes = {};

        if (!vm.isViewing) {
            vm.montantEnvoye = 0;
            vm.operation.montantTaxe = 0;
            vm.operation.montantOpEnDevise;
            vm.operation.uoAcquereurId = null;
        }
        vm.defaultDevise = {
            deviseId: null
        }
        vm.deviseP = {
            deviseLibelle: "",
            deviseId: null
        };
        vm.devises = [];
        vm.tauxChange = {
            taux: 1
        };

        vm.beneficiaireInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {
                typePersonne: null
            },
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };

        vm.emetteurInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {},
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };

        vm.clear = function clear() {
            vm.isSaving = false;
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else {
                if (vm.typeService == "ACCOUNT_CASH") {
                    $state.reload("account-cash");
                } else if (vm.typeService == "ACCOUNT_ACCOUNT") {
                    $state.reload("account-account");
                }
            }
        };
        /*
                Annulation de l'opération
         */
        function doAnnulation(operation) {
            console.log(operation);
            CancelAlert.confirm(function () {
                  RequestUtils.AnnulationOperation.get({
                operationId:operation.id
            },function (datas) {
                if(datas){
                    console.log('Annulation de l\'opération');
                    $uibModalInstance.close(true);
                    Notification.success("Opération soumise pour annulation!");
                    $state.go("operation");
                }

            });
            });
          
        }


        vm.annulationOperationAvecFrais = function (avecFrais) {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationAccountAvecRetourFrais.get({
                    operationId: vm.operation.id,
                    avecFrais: avecFrais
                }, function (datas) {
                    if (datas != null) {
                        console.log('redressement s frais',datas);
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
                        console.log('redressement av frais',datas);
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!")
                    }
                });
            });
        }

        $scope.$watch("vm.operation.devisePaiementId", function (value) {
            if (angular.isDefined(value) && value != null && vm.operation.id == null) {
                vm.deviseP = _.find(vm.devises, {
                    'deviseId': value
                });
                vm.getTauxChange(vm.defaultDevise.deviseId, vm.deviseP.deviseId, "ACHAT");
            }
        });
        vm.isSaving = false;

        RequestUtils.UserOrganisation.get({}, function (data) {
            console.log("organisation de l'utilisateur");
            vm.userOrganisation = data;
        });

        $scope.$watch("vm.estCompteBanque", function (value) {
            vm.emetteurInfos.compteCBIDTO = {};
        });

        $scope.$watch("vm.estCompteBanqueB", function (id) {
            vm.beneficiaireInfos.compteCBIDTO = {};
        });


        vm.personneE = {
            typePersonne: 'PHYSIQUE'
        };
        vm.personneB = {
            typePersonne: 'PHYSIQUE'
        };

        OperationUtils.init($scope, vm, vm.typeService, $uibModalInstance);
        OperationUtils.initVerificationOrganisatioType(vm);
        vm.oldUoAcq=null;
        vm.oldUoEm=null;
        function save() {
            CancelAlert.confirm(function () {
                vm.isSaving = true;

                 vm.oldUoEm = vm.operation.uoEmetteurId;
                 vm.oldUoAcq = vm.operation.uoAcquereurId;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                console.log("enregistrement de l'operation");
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function (datas) {
                            var datas = {};
                            vm.beneficiaireInfos.personneDTO = vm.personneB;
                            vm.beneficiaireInfos.personnePhysiqueDTO = vm.personnePhysiqueB;
                            vm.beneficiaireInfos.personneMoraleDTO = vm.personneMoraleB;
                            vm.beneficiaireInfos.pieceDTO = vm.pieceB;

                            if (vm.typeService == "ACCOUNT_ACCOUNT") {
                                vm.beneficiaireInfos.clientDTO = vm.clientB;
                                if (vm.compteDTOB != null) {
                                    vm.beneficiaireInfos.compteCBIDTO = {
                                        compteDTO: vm.compteDTOB
                                    };
                                } else {
                                    if (vm.estCompteBanqueB == "OUI") {
                                        vm.beneficiaireInfos.compteCBIDTO = vm.comptesB;
                                    } else {
                                        vm.beneficiaireInfos.compteCBIDTO = {};
                                        vm.beneficiaireInfos.compteCBIDTO.numeroCompte = vm.refCompteB;
                                        vm.beneficiaireInfos.compteCBIDTO.type = "Autre";
                                    }
                                }
                            } else {
                                vm.beneficiaireInfos.compteCBIDTO = null;
                                vm.beneficiaireInfos.clientDTO = null;
                            }


                            vm.emetteurInfos.personneDTO = vm.personneE;
                            vm.emetteurInfos.personnePhysiqueDTO = vm.personnePhysiqueE;
                            vm.emetteurInfos.personneMoraleDTO = vm.personneMoraleE;
                            vm.emetteurInfos.pieceDTO = vm.pieceE;
                            vm.emetteurInfos.clientDTO = vm.client;
                            //Pour les comptes banquaires de CBI
                            if (vm.compteDTOE != null) { //Informations des clients ayant souscrits
                                vm.emetteurInfos.compteCBIDTO = {
                                    compteDTO: vm.compteDTOE
                                }
                            } else {
                                if (vm.estCompteBanque == "OUI") {
                                    vm.emetteurInfos.compteCBIDTO = vm.comptes;
                                } else {
                                    vm.emetteurInfos.compteCBIDTO = {};
                                    vm.emetteurInfos.compteCBIDTO.numeroCompte = vm.refCompte;
                                    vm.emetteurInfos.compteCBIDTO.type = "Autre";
                                }
                            }

                            vm.operation.canalLibelleCanal = "WEB";
                            if (vm.typeService == "ACCOUNT_ACCOUNT") {
                                vm.operation.uoEmetteurId = vm.fseSrc;
                                vm.operation.uoAcquereurId = vm.fseDest;
                            } else {
                                vm.operation.uoEmetteurId = vm.fseSrc;
                                vm.operation.uoAcquereurId = vm.userOrganisation.fournisseurId;
                            }


                            datas.operationDTO = vm.operation;
                            datas.beneficiaire = vm.beneficiaireInfos;
                            datas.emetteur = vm.emetteurInfos;
                            datas.typeService = vm.typeService;
                            if (vm.typeService == "ACCOUNT_CASH") {
                                datas.typeBeneficiaire = vm.typeBeneficiaire;
                            }
                            RequestUtils.OperationVerifCumul.save(datas, function (result) {
                                console.log(result);
                                if (result != null) {
                                    switch (result.action) {
                                        case "BLOQUE":
                                            Notification.error(result.message + "!");
                                            break;
                                        case "NON_BLOQUANT":
                                            CancelAlert.confirm(function () {
                                                datas.operationDTO.statut = "ENATTENTE_VALIDATION";
                                                saveNow(datas);
                                            }, function () {
                                                vm.isSaving = false;
                                                initUo();
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
                                vm.isSaving = false;
                                initUo();
                            });
                        }, function () {
                            vm.isSaving = false;
                            initUo();
                        });
                    }).error(function () {
                        Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                    });
            });
        }

        function initUo(){
            if (vm.typeService == "ACCOUNT_ACCOUNT") {
                vm.operation.uoEmetteurId = vm.oldUoEm;
                vm.operation.uoAcquereurId = vm.oldUoAcq;
            }
        }

        function saveNow(datas) {
            RequestUtils.OperationMad.save(datas, function (result) {
                vm.isSaving = false;
                if (vm.typeService == "ACCOUNT_CASH") {
                    $state.reload("account-cash");
                    Notification.success("Appel de fonds effectué");
                } else {
                    $state.reload("account-account");
                    Notification.success("Virement effectué");
                }
                Utils.postPrintUrl(vm.url, {
                    'operationId': result.id
                });
            }, function () {
                vm.isSaving = false;
                initUo();
            });
        }

        vm.setBordereau = function ($file, operation) {
            if ($file) {
                DataUtils.toBase64($file, function (base64Data) {
                    $scope.$apply(function () {
                        operation.bordereau = base64Data;
                        operation.bordereauContentType = $file.type;
                    });
                });
            }
        };

        vm.traitementOperation = function () {
            console.log('il');
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash-traitement.html',
                controller: 'OperationTraitementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                $uibModalInstance.close(true);
            });
        };

        vm.steps = [
            'Information',
            'Expéditeur',
            'Bénéficiaire',
            'Documentation'
        ];
        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }
        var datas = {
            vm: vm,
            steps: vm.steps
        };
        InitSteps.initSteps(datas);

        /**
         * Mise à jour d'une operation
         */
        vm.majOperation = function (operation) {
            CancelAlert.confirm(function () {
                console.log("Mise à jour de l'operation");
                RequestUtils.OperaionMaj.update(operation, function (operation) {
                    console.log(operation);
                    $uibModalInstance.close(true);
                    Notification.success("Mise à jour effectuée!");
                    $state.go("operation");

                });
            });
        };

        vm.rembourse = function (operation) {
            console.log("rembourse appel de fond");
            CancelAlert.confirm(function () {
                console.log("Mise à jour de l'operation");
                RequestUtils.AnnulationAppelFond.get({
                    "operationId": operation.id
                }, function (operation) {
                    console.log(operation);
                    // $uibModalInstance.close(true);
                    Notification.success("Mise à jour effectuée!");

                });
            });
        };

        vm.choisirUo = function () {
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
        }

        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.operation.lieuPaiementId = result.id;
            }
        });


    }
})();
