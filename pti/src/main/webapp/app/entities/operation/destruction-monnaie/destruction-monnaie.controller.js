(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DestructionMonnaieController', DestructionMonnaieController);

    DestructionMonnaieController.$inject = ['$http','$sessionStorage', '$uibModalInstance', 'Utils', 'BASE_URL', 'isViewing', 'TypeUniteOrganisation', 'UniteOrganisation', 'CancelAlert', '$state', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', '$scope', '$rootScope', '$stateParams', 'entity', ];

    function DestructionMonnaieController($http,$sessionStorage, $uibModalInstance, Utils, BASE_URL, isViewing, TypeUniteOrganisation, UniteOrganisation, CancelAlert, $state, OperationUtils, Notification, RequestUtils, InitSteps, $scope, $rootScope, $stateParams, entity) {
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
                $state.reload("approvisionnement");
        }
        vm.organisationAgence = [];
        vm.organisationFSE = [];
        vm.organisationMaster = [];
        vm.organisationSousDistributeur = [];
        vm.steps = [
            'Information'
        ];

        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }
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
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {
                            console.log("Creation de la monnaie");
                        
        
                            vm.operation.uoAcquereurId = vm.fseId;
                                vm.operation.code = "DESTRUCTION_MONNAIE";
                            
                            vm.operation.canalLibelleCanal = "WEB";
                            RequestUtils.CreationMonnaieCompte.save(vm.operation, function (result) {
                                Notification.success("Destruction de la monnaie effectuée");
                                Utils.postPrintUrl(vm.url, {
                                    'operationId': result.id,
                                    'codeOperation': result.codeOperation
                                });
                                $state.go("operation");
                            });
                        });
                    }).error(function () {
                        Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
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