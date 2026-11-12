(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RetraitMembreController', RetraitMembreController);

    RetraitMembreController.$inject = ['$sessionStorage','$uibModalInstance', 'Utils', 'BASE_URL', 'isViewing', 'TypeUniteOrganisation', 'UniteOrganisation', 'CancelAlert', '$state', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', '$scope', '$rootScope', '$stateParams', 'entity', ];

    function RetraitMembreController($sessionStorage,$uibModalInstance, Utils, BASE_URL, isViewing, TypeUniteOrganisation, UniteOrganisation, CancelAlert, $state, OperationUtils, Notification, RequestUtils, InitSteps, $scope, $rootScope, $stateParams, entity) {
        var vm = this;
        vm.clear = clear;
        vm.save = save;
        vm.operation = entity;
        vm.isViewing = isViewing;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";

        function clear() {
            vm.isSaving = false;
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else
                $state.reload("operation-envoi-cash");
        }
        vm.organisationAgence = [];
        vm.organisationFSE = [];
        vm.organisationMaster = [];
        vm.organisationSousDistributeur = [];
        vm.steps = [
            'Information'
        ];


        if (vm.operation.id != null) {
            console.log("init all");
            initUO(vm.operation.uoAcquereurId);
        }

        function initUO(uoId) {
            RequestUtils.GetUoPereByType.get({
                uoId: uoId,
                libelleTypeUO: "PAYS"
            }, function (datas) {
                vm.paysId = datas.id;
            });

            RequestUtils.GetUoPereByType.get({
                uoId: uoId,
                libelleTypeUO: "FSE"
            }, function (datas) {
                vm.fseId = datas.id;
            });

            UniteOrganisation.get({
                id: uoId
            }, function (datas) {
                vm.typeUOId = datas.typeUOId;
                TypeUniteOrganisation.get({
                    id: vm.typeUOId
                }, function (datas) {
                    vm.typeActeur = datas.libelleTypeUO;
                    if (vm.typeActeur == 'AGENCE') {
                        vm.agenceId = uoId;
                    }


                    if (vm.typeActeur == 'MASTER') {
                        RequestUtils.GetUoPereByType.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        vm.masterId = uoId;
                    }

                    if (vm.typeActeur == 'SOUS_DISTRIBUTEUR') {
                        RequestUtils.GetUoPereByType.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        RequestUtils.GetUoPereByType.get({
                            uoId: uoId,
                            libelleTypeUO: "MASTER"
                        }, function (datas) {
                            vm.masterId = datas.id;
                        });
                        vm.sousDistributeurId = uoId;
                    }

                });
            });
        }

        OperationUtils.initVerificationOrganisatioType(vm);
        var datas = {
            vm: vm,
            steps: vm.steps
        };

        vm.organisationColums = [{
            name: 'libelleUniteOrganisation',
            title: 'Libelle'
        }];

        $scope.$on("onLoadOrganisation", function () {

        });

        $scope.$watch("vm.paysId", function (id) {
            vm.organisationFSE = getUoFils("FSE", id);
        });

        $scope.$watch("vm.fseId", function (id) {
            getUoFils("AGENCE", id);
        });

        $scope.$watch("vm.agenceId", function (id) {
            getUoFils("MASTER", id);
        });

        $scope.$watch("vm.masterId", function (id) {
            getUoFils("SOUS_DISTRIBUTEUR", id);
        });

        function getUoFils(type, id) {
            if (id != "" && type != "" && id != null && type != null) {
                console.log("recherche fils");
                RequestUtils.GetUoFilsByType.get({
                    type: type,
                    uoId: id
                }, function (datas) {
                    switch (type) {
                        case "FSE":
                            vm.organisationFSE = datas;
                            break;
                        case "AGENCE":
                            vm.organisationAgence = datas;
                            break;
                        case "MASTER":
                            vm.organisationMaster = datas;
                            break;
                        case "SOUS_DISTRIBUTEUR":
                            vm.organisationSousDistributeur = datas;
                            break;
                    }
                    console.log("organisation pays");
                    return datas;
                });
            } else {
                switch (type) {
                    case "FSE":
                        vm.paysId = null;
                        break;
                    case "AGENCE":
                        vm.fseId = null;
                        break;
                    case "MASTER":
                        vm.agenceId = null;
                        break;
                    case "SOUS_DISTRIBUTEUR":
                        vm.agenceId = null;
                        break;
                }
            }
        }

        function save() {
            CancelAlert.confirm(function () {
                RequestUtils.VerifToken.get({
                    token: vm.token,
                    loginData: $sessionStorage.infos
                }, function () {
                    vm.operation.canalLibelleCanal = "WEB";
                    if (vm.typeActeur == "MASTER") {
                        vm.operation.uoAcquereurId = vm.masterId;
                    } else if (vm.typeActeur == "SOUS_DISTRIBUTEUR") {
                        vm.operation.uoAcquereurId = vm.sousDistributeurId;
                    } else {
                        vm.operation.uoAcquereurId = vm.agenceId;
                    }
                    RequestUtils.OperationRetraitCompte.save(vm.operation, function (result) {
                        Notification.success("Approvisionnement effectué");
                        Utils.postPrintUrl(vm.url, {
                            'operationId': result.id
                        });
                        $state.go("operation");
                    });
                });
            });
        }

        RequestUtils.GetOrganisationByType.get({
            type: "PAYS"
        }, function (datas) {
            console.log("organisation pays");
            vm.organisationPays = datas;
        });
        OperationUtils.init($scope, vm, vm.typeService, vm.operation);
        InitSteps.initSteps(datas);
    }
})();