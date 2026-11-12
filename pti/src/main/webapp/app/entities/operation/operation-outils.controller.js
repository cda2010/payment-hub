(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationOutilsController', OperationOutilsController);

    OperationOutilsController.$inject = ['OperationUtils', '$scope', 'entity','RequestUtils','$state'];

    function OperationOutilsController(OperationUtils, $scope, entity,RequestUtils,$state) {
        var vm = this;
        //  OperationUtils.init($scope, vm, vm.typeService, vm.operation);

        vm.taxes = [];
        vm.operation = entity;

        if (!vm.isViewing) {
            vm.montantEnvoye = 0;
            vm.operation.montantTaxe = 0;
            vm.operation.montantOpEnDevise;
            vm.operation.uoAcquereurId = null;
        }
        vm.typeservices = RequestUtils.ServicesFrais.query();
        vm.montantTtc = null;
        vm.defaultDevise = {
            deviseId: null
        }
        vm.message = null;
        vm.deviseP = {
            deviseLibelle: "",
            deviseId: null
        };

        vm.devises = [];
        vm.tauxChange = {
            taux: 1
        }

        //Suivi de modification de l'uoAcq
        $scope.$watch("vm.operation.uoAcquereurId", function (value) {
            if (angular.isDefined(value) && value != null) {
                vm.getAllDevise(value);
            }
        });

        vm.canCalcul = function (uoId) {
            if ((angular.isDefined(uoId) && angular.isDefined(vm.operation.montantOp)) ||
                angular.isDefined(uoId) && angular.isDefined(vm.montantTtc)) {
                return ((uoId != null && vm.operation.montantOp != null) ||
                    (uoId != null && vm.montantTtc != null));
            }
            return false;
        }

        vm.simulateur=function(){
            console.log('simulateur de frais');
            $state.go('simulation-operation');
        }

        /**
            Recherche du tableau de change 
        */
        $scope.$watch("vm.operation.devisePaiementId", function (value) {
            if (angular.isDefined(value) && value != null && vm.operation.id == null) {
                vm.deviseP = _.find(vm.devises, {
                    'deviseId': value
                });
                vm.getTauxChange(vm.defaultDevise.deviseId, vm.deviseP.deviseId, "ACHAT");
            }
        });

        vm.annulerCalcul = function () {
            vm.isFraisDispo = false;
            vm.operation.montantFrais = null;
            vm.montantOp =null;
            vm.operation.montantTaxe = 0;
            vm.montantTtc = 0;
            vm.operation.montantOp =null;
            vm.montantTtc = null;
            vm.message = null;
        }

        $scope.$watch("vm.operation.montantOp", function () {
            vm.operation.montantOpEnDevise = vm.operation.montantOp * vm.tauxChange.taux;
        });

        RequestUtils.GetOrganisationByType.get({
            type: "PAYS"
        }, function (datas) {
            vm.pays = datas;
        });
        // vm.typeService="ENVOI_CASH";

        function calculFrais(uoId, uoEmId) {
            if (angular.isDefined(uoId) && angular.isDefined(vm.operation.montantOp)) {
                RequestUtils.CalculFrais.get({
                    canalLibelle: "WEB",
                    uoAcq: uoId,
                    typeServiceLibelle: vm.typeService,
                    montant: vm.operation.montantOp,
                    uoEmId: uoEmId
                }, function (data) {
                    vm.message = data.message;
                    vm.montantOp = vm.operation.montantOp;
                    vm.isFraisDispo = true;
                    if (data != null) {
                        if (data.valeur != null) {
                            vm.operation.montantFrais = data.valeur;
                        } else {
                            vm.operation.montantFrais = 0;
                        }

                        if (data.typeFrais == "FIXE") {
                            vm.montantOp = vm.operation.montantOp + vm.operation.montantFrais;
                        } else if (data.typeFrais == "POURCENTAGE") { //POURCENTAGE
                            vm.operation.montantFrais = vm.roundedPercentage(data.valeur, vm.montantOp);
                            vm.montantOp = vm.operation.montantOp + vm.operation.montantFrais;
                        }
                    }
                    //Calcul de la tva
                    RequestUtils.getTaxes.get({
                        organisationId: uoId,
                        service: vm.typeService,
                        montantOp: vm.operation.montantOp,
                        montantFrais: vm.operation.montantFrais
                    }, function (data) {
                        angular.forEach(data, function (value, index) {
                            vm.operation.montantTaxe = value.montant + vm.operation.montantTaxe;
                            if (value.appliqueSur == "MONTANT") {
                                // vm.operation.montantFrais=vm.operation.montantFrais+value.montant;
                            } else if (value.appliqueSur == "FRAIS") {
                                vm.operation.montantFrais = vm.operation.montantFrais - value.montant;
                            }
                        });
                        vm.montantTtc = vm.operation.montantOp + vm.operation.montantFrais + vm.operation.montantTaxe;

                        vm.taxes = data;
                    });

                    //Recuperation du taux de change et conversion
                    if (vm.typeService != "ENVOI_CASH")
                        vm.getTauxChange(vm.defaultDevise.deviseId, vm.deviseP.deviseId, "ACHAT");

                });
            } else {}
        }

        function calculFraisSimulation(uoId, uoEmId) {
            if (angular.isDefined(uoId)) {
                RequestUtils.SimulationCalculFrais.get({
                    canalLibelle: "WEB",
                    uoAcq: uoId,
                    typeServiceLibelle: vm.typeService,
                    montant: vm.montantTtc,
                    uoEmId: uoEmId
                }, function (data) {
                    console.log("resultat de la simulation");
                    vm.operation.montantOp = data.montantOp;
                    vm.operation.montantFrais = data.montantFrais;
                    vm.isFraisDispo = true;
                    vm.message = data.commentaire;
                    //Calcul de la tva
                    RequestUtils.getTaxes.get({
                        organisationId: uoId,
                        service: vm.typeService,
                        montantOp: vm.operation.montantOp,
                        montantFrais: vm.operation.montantFrais
                    }, function (data) {
                        angular.forEach(data, function (value, index) {
                            vm.operation.montantTaxe = value.montant + vm.operation.montantTaxe;
                            if (value.appliqueSur == "MONTANT") {
                                // vm.operation.montantFrais=vm.operation.montantFrais+value.montant;
                            } else if (value.appliqueSur == "FRAIS") {
                                vm.operation.montantFrais = vm.operation.montantFrais - value.montant;
                            }
                        });
                        vm.montantTtc = vm.operation.montantOp + vm.operation.montantFrais + vm.operation.montantTaxe;
                        vm.taxes = data;
                    });
                });
            } else {}
        }

        vm.calcul = function (uoId, uoEmId) {
            if (vm.operation.montantOp != null) calculFrais(uoId, uoEmId);
            else if (vm.montantTtc != null) calculFraisSimulation(uoId, uoEmId);
        }
        OperationUtils.init($scope, vm, vm.typeService, vm.operation);
        OperationUtils.initVerificationOrganisatioType(vm);
    }
})();