(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PaiementController', PaiementController);

    PaiementController.$inject = ['$http', '$sessionStorage', 'ChoisirUo', '$uibModalInstance', 'isMaj', 'BASE_URL', 'Utils', 'CancelAlert', '$uibModal', 'isViewing', 'DataUtils', 'typeService', 'DateUtils', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', '$scope', '$rootScope', 'entity', '$state'];

    function PaiementController($http, $sessionStorage, ChoisirUo, $uibModalInstance, isMaj, BASE_URL, Utils, CancelAlert, $uibModal, isViewing, DataUtils, typeService, DateUtils, OperationUtils, Notification, RequestUtils, InitSteps, $scope, $rootScope, entity, $state) {
        var vm = this;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.operation = entity;
        console.log('operation',vm.operation);
        vm.typeService = typeService;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.isViewing = isViewing;
        vm.estCompteBanque = "OUI";
        vm.estCompteBanqueB = "OUI";
        vm.isMaj = isMaj;

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
        };
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
                if (vm.typeService === "ACCOUNT_CASH") {
                    $state.reload("account-cash");
                } else if (vm.typeService === "ACCOUNT_ACCOUNT") {
                    $state.reload("account-account");
                }
            }
        };


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


        function initUo(){
            if (vm.typeService === "ACCOUNT_ACCOUNT") {
                vm.operation.uoEmetteurId = vm.oldUoEm;
                vm.operation.uoAcquereurId = vm.oldUoAcq;
            }
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


        vm.steps = [
            'Information',
            'Expéditeur',
            'Bénéficiaire',
            'Organisation Acquereur',
            'Organisation Emetteur'
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
                    //$uibModalInstance.close(true);
                    Notification.success("Mise à jour effectuée!");

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
        };

        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.operation.lieuPaiementId = result.id;
            }
        });


    }
})();
