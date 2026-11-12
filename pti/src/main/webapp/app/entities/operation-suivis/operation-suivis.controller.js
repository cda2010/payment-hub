(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationSuivisController', OperationSuivisController);

    OperationSuivisController.$inject = ['$state', 'TableService','$scope','OperationSuivis', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', 'Nationalite', 'BlackList','Client', 'Notification'];

    function OperationSuivisController($state, TableService,$scope,OperationSuivis, ParseLinks, AlertService, paginationConstants, pagingParams, Nationalite, BlackList,Client, Notification) {

        var vm = this;
        vm.tableau = false;
        vm.afficheFiltreO = false;
        vm.afficherFiltreO = afficherFiltreO;
        vm.chercherO = chercherO;
        vm.afficher= afficher;
        vm.generateListe=generateListe;
        vm.nationalites = Nationalite.query();
        vm.clients=[];
        vm.clients1=[];
        vm.ctrlData1=false;
        vm.ctrlData1=false;
        vm.ctrlData3=true;
        vm.id=null;
        vm.totalItems = 0;
        vm.queryCount = 0;
        vm.page = 1;
        vm.itemsPerPage=20;
        vm.clientColums = [{
            title: 'Nom',
            name: 'title'
        }];
        vm.operationSuivis=OperationSuivis.query();
        console.log('toksa',vm.operationSuivis);
        vm.setItemsPerPage=function(nombre){
            vm.itemsPerPage=nombre;
            afficher();
        };
        // vm.clients = BlackList.queryAll( function (result) {
        //     vm.clients=result;
        //     console.log('new requete',vm.clients);
        // });

        vm.itemSearch = {
            'national_id':null,
            'pnomCli':null,
            'tel':null
        };
        $scope.$watch("vm.page", function() {
            afficher();
        });

        $scope.$watch("vm.clientIds", function (ids) {
            if (angular.isDefined(ids)) {
                var tests;
                tests = _.find(vm.clientPList, {'id': ids});
                vm.clientFinal= tests;
                vm.clientBId = tests.id;
                vm.operationSuivis.clientId =vm.clientBId;
            }

        });
        function afficherFiltreO() {
            vm.tableau = false;
            vm.afficheFiltreO= true;
            vm.itemSearch = {
                'national_id':null,
                'nomCli':null,
                'tel':null
            };
            vm.operationSuivis.clientId = null;
            vm.operationSuivis.clientIdent = null;
            vm.clientIds=null;
            vm.clients=null;
            vm.ctrlData1=false;
            vm.ctrlData2=false;
            vm.ctrlData3=true;
            vm.clientPList=null;
        }

        function afficher() {
            console.log('id',vm.operationSuivis.clientId);
            if (vm.operationSuivis.clientId) {
                vm.id = vm.operationSuivis.clientId;
                vm.tableau = true;
               OperationSuivis.findOperaSuivisByClient({
                   id:vm.operationSuivis.clientId,
                   page: vm.page - 1,
                   size: vm.itemsPerPage
                }, function (reponse, headers) {

                   vm.newOperationSuivis = reponse;
                   vm.totalItems = headers('X-Total-Count');
                   vm.queryCount = reponse.length;
                   console.log(vm.totalItems,vm.queryCount);
                   console.log(vm.newOperationSuivis);
            });
            }
        }
        function  generateListe() {

        }
        function chercherO() {

            console.log(vm.itemSearch.national_id);
            console.log(vm.itemSearch.nomCli);
            console.log(vm.itemSearch.tel);
            Client.findOpListeClient({
                typePersonne:'PHYSIQUE',
                telephone:vm.itemSearch.tel,
                nomClient: vm.itemSearch.nomCli,
                code:'226'
            },function (data) {
                console.log('data',data);
                if(data){
                    vm.ctrlData1=true;
                    vm.ctrlData2=true;
                    var i=0;
                    vm.clientP=[];
                    vm.clientPList=[];
                    angular.forEach(data,function (value,index) {
                        var v = {};
                        v.name = value.personnePhysiqueDTO.nomClient;
                        v.prenom = value.personnePhysiqueDTO.prenomClient;
                        i++;
                        v.id = value.clientDTO.id;
                        v.title = value.personnePhysiqueDTO.nomClient+' '+value.personnePhysiqueDTO.prenomClient;
                        vm.clientPList.push(v);
                        vm.clientP.push(v);
                    });
                }
            });
           /* if (vm.itemSearch.national_id  && !vm.itemSearch.tel && !vm.itemSearch.nomCli ){
                BlackList.findByCode({id: vm.itemSearch.national_id}, function (data) {
                    console.log('par nationalité',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });

                    }

                })
            }

            if (vm.itemSearch.tel && !vm.itemSearch.national_id && !vm.itemSearch.nomCli  ){
                BlackList.findByTel({id: vm.itemSearch.tel}, function (data) {
                    console.log('par tel',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }

                })
            }
            if (vm.itemSearch.nomCli && !vm.itemSearch.tel && !vm.itemSearch.national_id ){
                OperationSuivis.findByNom({nom: vm.itemSearch.nomCli}, function (data) {
                    console.log('par nom',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }

                })
            }
            if (vm.itemSearch.national_id && vm.itemSearch.tel  && !vm.itemSearch.nomCli )
            {
                BlackList.findByCodeAndTel({id1: vm.itemSearch.national_id , id2: vm.itemSearch.tel }, function (data) {
                    console.log('par code et tel',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }

                })
            }
            if (vm.itemSearch.national_id && vm.itemSearch.nomCli && !vm.itemSearch.tel) {
                OperationSuivis.findByCodeAndNom({code: vm.itemSearch.national_id , nom: vm.itemSearch.nomCli }, function (data) {
                    console.log('par code et nom',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }

                })
            }
            if (vm.itemSearch.tel && vm.itemSearch.nomCli && !vm.itemSearch.national_id) {
                OperationSuivis.findByTelAndNom({tel: vm.itemSearch.tel , nom: vm.itemSearch.nomCli}, function (data) {
                    console.log('par tel et nom',data);
                    if(data){
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }


                })
            }
            if (vm.itemSearch.national_id && vm.itemSearch.tel && vm.itemSearch.nomCli) {
                OperationSuivis.findByCodeAndTelAndNom({code:vm.itemSearch.national_id,  tel: vm.itemSearch.tel , nom: vm.itemSearch.nomCli},function (data) {
                    console.log('par code, tel et nom',data);
                    if(data) {
                        vm.ctrlData1=true;
                        vm.ctrlData2=true;
                        var i=0;
                        vm.clientP=[];
                        vm.clientPList=[];
                        angular.forEach(data,function (value,index) {
                            var v = {};
                            v.name = value.personne.personnePhysique.nomClient;
                            v.prenom = value.personne.personnePhysique.prenomClient;
                            i++;
                            v.id = value.id;
                            v.title = value.personne.personnePhysique.nomClient+' '+value.personne.personnePhysique.prenomClient;
                            vm.clientPList.push(v);
                            vm.clientP.push(v);
                        });
                        console.log(vm.clientP)
                    }

                })
            }
        */

        }
    }
})();
