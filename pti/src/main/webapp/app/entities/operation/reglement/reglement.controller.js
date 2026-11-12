(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('ReglementController', ReglementController);

    ReglementController.$inject = ['$timeout', 'ParametreApp', '$scope', '$rootScope', '$stateParams', 'Principal', '$http', '$sessionStorage', '$uibModalInstance', 'Utils', 'BASE_URL', 'isViewing', 'TypeUniteOrganisation', 'UniteOrganisation', 'Regulation', 'CancelAlert', '$state', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', 'entity'];

    function ReglementController($timeout, ParametreApp, $scope, $rootScope, $stateParams, Principal, $http, $sessionStorage, $uibModalInstance, Utils, BASE_URL, isViewing, TypeUniteOrganisation, UniteOrganisation, Regulation, CancelAlert, $state, OperationUtils, Notification, RequestUtils, InitSteps, entity) {
        var vm = this;
        vm.clear = clear;
        vm.isCheckCorebanking = false;
        vm.save = save;
        vm.operation = entity;
        vm.operationReglementDTO = {operationDto: null, uoId: null};
        vm.isViewing = isViewing;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.FSEs = UniteOrganisation.queryByTypeUoFSE();
        vm.typeuniteorganisations = [];
        vm.agenceList = [];
        vm.agents = [];
        vm.listeAgents = [];
        vm.agenceFetch = agenceFetch;
        vm.appro = {
            'pereUOId': null,
            'libelleUniteOrganisation': '',
            'codeUO': '',
            'numeroOrdre': null,
            'typeUO': null,
            'pereUoId': null
        };
        vm.organisationColums = [{
                title: 'libelleUniteOrganisation',
                name: 'title'
            }];


        TypeUniteOrganisation.query(function (typeUniteOrganisations) {
            let i = 0;
            for (i = 0; i < typeUniteOrganisations.length; i++) {
                if (typeUniteOrganisations[i].libelleTypeUO != "PAYS"
                        && typeUniteOrganisations[i].libelleTypeUO != "FSE"
                        && typeUniteOrganisations[i].libelleTypeUO != "ZONE") {
                    vm.typeuniteorganisations.push(typeUniteOrganisations[i]);
                }
            }
        });

        function agenceFetch() {


            console.log('liste', vm.agents);
            console.log('appro', vm.typeActeur, vm.fseId, vm.appro.typeUO, vm.appro.pereUoId);
            if (vm.appro.typeUO) {
                Regulation.queryAllUO({
                    libelleUniteOrganisation: vm.appro.libelleUniteOrganisation
                    , codeUO: vm.appro.codeUO, typeUO: vm.appro.typeUO, pereUoId: vm.appro.pereUoId
                }, function (response) {

                    var i = 0;
                    if (response) {
                        vm.agents = [];
                        vm.agenceList = [];
                        angular.forEach(response, function (value, index) {
                            var v = {};
                            v.name = value.pereUOLibelleUniteOrganisation;
                            i++;
                            v.id = value.id;
                            v.title = value.libelleUniteOrganisation;
                            vm.agenceList.push(v);
                            vm.agents.push(value.libelleUniteOrganisation);
                        });
                        console.log('response',vm.agenceList)
                    }

                });
            }
        }

        $scope.$watch("vm.agenceIds", function (ids) {
            console.log('---start---', ids);
            vm.auth2 = [];
            console.log('---end---');
            if (angular.isDefined(ids)) {
                vm.agenceId = [];
                vm.agenceId2 = [];
                console.log('liste ag',vm.agenceList);
                angular.forEach(ids, function (value, index) {
                    console.log('val',value);
                    var test;
                    test = _.find(vm.agenceList, {'id': value});
                    console.log(test);
                    if (test) {
                        vm.agenceId2.push(test.id);
                        vm.agenceId.push(test.title);
                    }
                });
            }

            vm.verifyListAgence = vm.agenceId2;
            vm.verifyListAgence2 = vm.agenceId;
        });
        console.log('recap', vm.verifyListAgence);
        function clear() {
            vm.isSaving = false;
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else
                $state.reload("reglement");
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


        OperationUtils.initVerificationOrganisatioType(vm);
        var datas = {
            vm: vm,
            steps: vm.steps
        };


        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });






        function save() {

            CancelAlert.confirm(function () {
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                        .success(function (data, status, headers, config) {
                            RequestUtils.VerifToken.get({
                                token: vm.token,
                                loginData: JSON.stringify(data)
                            }, function () {
                                
                                
                                
                                 Principal.identity().then(function (account) {
                                    vm.operation.login = account.login;
              
               
            });
                                

                                vm.operation.code = "REGLEMENT";
                                vm.operation.canalLibelleCanal = "WEB";


                                vm.operationReglementDTO = {operationDto: vm.operation, uoId: vm.verifyListAgence};
                                RequestUtils.ReglementCompte.save(vm.operationReglementDTO, function (result) {
                                    console.log('reponse',result);
                                    Notification.success("Approvisionnement effectué");
                            /*        Utils.postPrintUrl(vm.url, {
                                        'operationId': result.id
                                    });*/
                                   // $state.go("operation");
                                });


                            });
                        }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });

        }
       

        //OperationUtils.init($scope, vm, vm.typeService, vm.operation);
        InitSteps.initSteps(datas);
    }
})();
