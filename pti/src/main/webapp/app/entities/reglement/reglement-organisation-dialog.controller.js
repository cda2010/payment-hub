(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('ReglementOrganisationDialogController', ReglementOrganisationDialogController);

    ReglementOrganisationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'Utils', 'Principal', '$http', '$sessionStorage', 'DateUtils', 'BASE_URL', 'entity', 'ServiceProfil', 'TypeUniteOrganisation', 'UniteOrganisation', 'Regulation', 'CancelAlert', 'OperationUtils', 'Notification', 'RequestUtils', 'InitSteps', 'isViewing', '$state', 'ReglementOrganisation'];

    function ReglementOrganisationDialogController($timeout, $scope, $stateParams, $uibModalInstance, Utils, Principal, $http, $sessionStorage, DateUtils, BASE_URL, entity, ServiceProfil, TypeUniteOrganisation, UniteOrganisation, Regulation, CancelAlert, OperationUtils, Notification, RequestUtils, InitSteps, isViewing, $state, ReglementOrganisation) {
        var vm = this;
        var test;
        vm.clear = clear;
        vm.isCheckCorebanking = false;
        vm.save = save;
        vm.operation = entity;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.operationReglementDTO = {operationDto: null, uoId: null, dateReglement: null};
        vm.isViewing = isViewing;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.FSEs = UniteOrganisation.queryByTypeUoFSE();
        vm.typeuniteorganisations = [];
        vm.agenceList = [];
        vm.agents = [];
        vm.listeAgents = [];
        vm.typeRecherche = null;
        vm.selectedTypeAc = false;
        vm.selectedTypeProf = false;
        vm.serviceProfilId = null;
        vm.agenceFetch = agenceFetch;
        vm.uniteOrgFetch = uniteOrgFetch;
        vm.selectTypeRech = selectTypeRech;
        vm.ctrlSaved = true;
        vm.isSaving=false;
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
        vm.reglement = {
            dateReglement: null
        };

        vm.seviceProfiles = ServiceProfil.queryByType({type: 'ORGANISATION'});


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

        vm.datePickerOpenStatus.dateReglement = false;

        function selectTypeRech() {
            console.log('------------Selected----');
            console.log(vm.typeRecherche);
            if (vm.typeRecherche === 'typeActeur') {
                vm.selectedTypeAc = true;
                vm.selectedTypeProf = false;
            } else if (vm.typeRecherche === 'Service') {
                vm.selectedTypeProf = true;
                vm.selectedTypeAc = false;
            }
        }

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

        function uniteOrgFetch() {
            console.log('--Profil', vm.serviceProfilId);
            if (vm.serviceProfilId != null) {
                console.log('--Profil id--', vm.serviceProfilId);
                UniteOrganisation.queryByServiceProfil({profilServiceId: vm.serviceProfilId, pereUoId: vm.appro.pereUoId}, function (reponse) {
                    console.log('result', reponse);
                    var i = 0;
                    if (reponse) {
                        vm.agents = [];
                        vm.agenceList = [];
                        angular.forEach(reponse, function (value, index) {
                            var v = {};
                            v.name = value.pereUOLibelleUniteOrganisation;
                            i++;
                            v.id = value.id;
                            v.title = value.libelleUniteOrganisation;
                            vm.agenceList.push(v);
                            vm.agents.push(value.libelleUniteOrganisation);
                        });
                    }
                }, function (error) {
                    console.log('erreur', error);
                });
            }

        }

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
                angular.forEach(ids, function (value, index) {

                    test = _.find(vm.agenceList, {'id': value});
                    vm.agenceId2.push(test.id);
                    vm.agenceId.push(test.title);
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
                $state.reload("reglement-organisation");
            $uibModalInstance.close(true);
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
                               
                                vm.operation.code = "REGLEMENT"; 
                                vm.operation.canalLibelleCanal = "WEB";
                                Principal.identity().then(function (account) {
                                    vm.operation.login = account.login;
                                });
                                vm.dateReglement = vm.reglement.dateReglement;


                                vm.operationReglementDTO = {operationDto: vm.operation, uoId: vm.verifyListAgence, dateReglement: vm.dateReglement};
                                console.log('opa', vm.operationReglementDTO);
                                vm.isSaving=true;
                                RequestUtils.ReglementCompte.save(vm.operationReglementDTO, function (result) {
                                    vm.ctrlSaved = false;
                                    vm.isSaving=true;
                                    console.log(result);
                                    Notification.success("Règlement effectué");
                                    $uibModalInstance.close(true);
                                    $state.go("reglement-organisation");
                                }, function (erreur) {
                                    console.log(erreur)
                                });
                                $state.go("reglement-organisation");

                            });
                            $state.go("reglement-organisation");
                        }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        
            console.log('Nooooooo', vm.ctrlSaved);

        }


        //OperationUtils.init($scope, vm, vm.typeService, vm.operation);
        InitSteps.initSteps(datas);
    }
})();
