(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ApprovisionnementController', ApprovisionnementController);

    ApprovisionnementController.$inject = ['$uibModal','ParametreApp','Principal','$http','$sessionStorage', '$uibModalInstance', 'Utils', 'BASE_URL', 'isViewing', 'TypeUniteOrganisation', 'UniteOrganisation', 'CancelAlert', '$state', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', '$scope', '$rootScope', '$stateParams', 'entity', ];

    function ApprovisionnementController($uibModal,ParametreApp,Principal,$http,$sessionStorage, $uibModalInstance, Utils, BASE_URL, isViewing, TypeUniteOrganisation, UniteOrganisation, CancelAlert, $state, OperationUtils, Notification, RequestUtils, InitSteps, $scope, $rootScope, $stateParams, entity) {
        var vm = this;
        vm.clear = clear;
        vm.isCheckCorebanking=false;
        vm.save = save;
        vm.operation = entity;
        vm.isViewing = isViewing;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";

 Principal.identity().then(function(account) {     
   
      RequestUtils.GetUoPereByTypeOptimise.get({ uoId: account.uniteOrganisationId,
                libelleTypeUO: "FSE" }, function (datas) {
                
                ParametreApp.queryIsCheckCoreBanking({codeFSE:datas.codeFse},function (donnee){
                    if (parseInt(donnee.valeur)===0){
                        vm.isCheckCorebanking=false;
                    }else{
                         vm.isCheckCorebanking=true;
                    }
                     console.log(angular.fromJson(donnee));
                });
           
      });
 });
 console.log('isCheckCorebanking',vm.isCheckCorebanking);
vm.traitementOperationC = function () {
           
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
                Notification.success("Validation effectué avec succès.");
                $uibModalInstance.close(true);
                //   $state.reload();
                $state.go('operation',{}, {reload: true});
            });
        };

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
//
        function initUO(uoId) {
            RequestUtils.GetUoPereByTypeOptimise.get({
                uoId: uoId,
                libelleTypeUO: "PAYS"
            }, function (datas) {
                vm.paysId = datas.id;
            });

            RequestUtils.GetUoPereByTypeOptimise.get({
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
                        RequestUtils.GetUoPereByTypeOptimise.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        vm.masterId = uoId;
                    }

                    if (vm.typeActeur == 'SOUS_DISTRIBUTEUR') {
                        RequestUtils.GetUoPereByTypeOptimise.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        RequestUtils.GetUoPereByTypeOptimise.get({
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



        
        


        $scope.$watch("vm.typeActeur", function (id) {
           getUoFilsFSE(vm.typeActeur, vm.fseId);
        });

        $scope.$watch("vm.agenceId", function (id) {
            getUoFils("MASTER", id);
        });

        $scope.$watch("vm.masterId", function (id) {
            getUoFils("SOUS_DISTRIBUTEUR", id);
        });
        
        
            function getUoFilsFSE(type, id) {
            if (id != "" && type != "" && id != null && type != null) {
                console.log("recherche fils");
                RequestUtils.OrganisationsFilsByTypeOptimiser.get({
                    type: type,
                    uoId: id
                }, function (datas) {
                    switch (type) {
                       
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
                            console.log("Approvisionnement de compte");
                            if (vm.typeActeur == "MASTER") {
                                vm.operation.uoAcquereurId = vm.masterId;
                            } else if (vm.typeActeur == "SOUS_DISTRIBUTEUR") {
                                vm.operation.uoAcquereurId = vm.sousDistributeurId;
                            } else {
                                vm.operation.uoAcquereurId = vm.agenceId;
                            }
        
                            if (vm.isMaster()) {
                                vm.operation.code = "APPROVISIONNEMENT_INTERNE";
                            }
                            vm.operation.canalLibelleCanal = "WEB";
                            RequestUtils.OperationApprovisionnementCompte.save(vm.operation, function (result) {
                                Notification.success("Approvisionnement effectué");
                                Utils.postPrintUrl(vm.url, {
                                    'operationId': result.id
                                });
                                $state.go("operation");
                            });
                        });
                    }).error(function () {
                        Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                    });
            });
        }

        RequestUtils.GetUoByTypeOptimiser.get({
            typeUo: "PAYS"
        }, function (datas) {
            console.log("organisation pays--****");
            vm.organisationPays = datas;
        });
        OperationUtils.init($scope, vm, vm.typeService, vm.operation);
        InitSteps.initSteps(datas);
    }
})();
