(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('OperationAgentController', OperationAgentController);

        OperationAgentController.$inject = ['BASE_URL_API', '$timeout','$scope', '$state',  '$stateParams','$http','OperationUtils','RequestUtils', 'Utils','InitSteps'];

        function OperationAgentController( BASE_URL_API, $timeout,$scope, $state,$stateParams, $http,OperationUtils,RequestUtils, Utils,InitSteps) {

            var vm = this;
            var URL = BASE_URL_API + "reporting/operations/service-details-by-uo";
            var test;
            vm.etatIdent=null;
            vm.etatClient=null;
            vm.save = save;
            vm.getEtatL = getEtatL;
            vm.getListeOrganisation=getListeOrganisation;
            vm.datePickerOpenStatus = {};
            vm.openCalendar = openCalendar;
            //vm.genererL=genererL;
            //vm.clear = clear;
            //vm.confirmCancel = confirmCancel;
            vm.listeAgents=null;
            vm.format = null;
            vm.typeActeurs=["FSE","AGENCE","MASTER","MARCHAND","SOUS_DISTRIBUTEUR"];
            vm.organisationAgence = [];
            vm.organisationFSE = [];
            vm.organisationMaster = [];
            vm.organisationRegroupement=[];
            vm.organisationSousDistributeur = [];
            vm.organisationMarchand=[];
            vm.typeActeurSelected=null;
            vm.regroupementId=null;
            vm.item={
                format: null,
                pereId: null,
                agentIds: null,
                dateDebut: null,
                dateFin: null,
                type: null
            };
            vm.etats = [
                {
                    ident:1,
                    libelle:'Liste des clients',
                    url: URL+'listing/client',
                    type:'CLIENT'

                }

            ];

            function getEtatL(id){
                vm.etat = _.find(vm.etats, {'ident': id});
                console.log(id);
            }
            vm.clear = function () {
                console.log('Canceled-----');
                vm.etatIdent = null;
                vm.etat = null;
                vm.etatClient=null;
            };
            vm.datePickerOpenStatus.dateDeut = false;
            vm.datePickerOpenStatus.dateFin = false;

            function openCalendar (date) {
                vm.datePickerOpenStatus[date] = true;
            }

            function save() {
                vm.item.pereId=vm.fseId;
                vm.item.format = 'PDF';
                vm.item.agentIds=vm.agenceId2;
                vm.item.dateDebut=vm.dateDebut;
                vm.item.dateFin=vm.dateFin;
                if(vm.type!=null){
                    vm.item.type=vm.type;
                } else {
                    vm.item.type='OAD';
                }
                console.log('OK',vm.item);
               Utils.postPrintUrl(URL, vm.item);
            }
            vm.steps = [
                'Organisations'
            ];

            if (!vm.isViewing) {
                vm.steps.push("Export");
            }
            RequestUtils.GetOrganisationByType.get({type: "PAYS"}, function (datas) {
                vm.organisationPays = datas;
            });

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


            $scope.$watch("vm.masterId", function (id) {
                getUoFils("SOUS_DISTRIBUTEUR", id);
            });

            $scope.$watch("vm.regroupementId", function (id) {
                getUoFils("MASTER", id);
            });


            $scope.$watch("vm.sousDistributeurId", function (ids) {
                if (angular.isDefined(ids)) {
                    vm.agenceId = [];
                    vm.agenceId2 = [];
                    angular.forEach(ids, function (value, index) {

                        test = _.find(vm.listAgence, {'id': value});
                        vm.agenceId2.push(test.id);
                        vm.agenceId.push(test.libelleUniteOrganisation);
                    });
                }
            });



            function getListeOrganisation(typeActeur) {
                RequestUtils.GetUoByTypeOptimiser.get({
                    typeUo: typeActeur,
                },function (datas) {
                    vm.organisationSousDistributeur=datas;
                    vm.listAgence=datas;
                })
            }

            function getUoFils(type, id) {
                vm.typeActeurSelected=type;
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
                            case "MARCHAND":
                                vm.organisationMarchand = datas;
                                vm.listMarchand=datas;
                                break;
                            case "UNITE_REGROUPEMENT":
                                vm.organisationRegroupement=datas;
                                break;
                            case "SOUS_DISTRIBUTEUR":
                                vm.organisationSousDistributeur = datas;
                                vm.listAgence=datas;
                                console.log('Sous distribut MAIGA',datas);
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
                        case "MARCHAND":
                            vm.agenceId = null;
                            break;
                        case "SOUS_DISTRIBUTEUR":
                            vm.agenceId = null;
                            break;
                        case "UNITE_REGROUPEMENT":
                            vm.agenceId = null;
                            break;
                    }
                }
            }

            OperationUtils.initVerificationOrganisatioType(vm);
            var datas = {
                vm: vm,
                steps: vm.steps
            };


            $timeout(function () {
                angular.element('.form-group:eq(1)>input').focus();
            });

            InitSteps.initSteps(datas);
        }
    }


)();
