(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BlackListDialogController', BlackListDialogController);

    BlackListDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance','$uibModal','Notification', 'entity', 'BlackList', 'Client','Nationalite','Personne'];

    function BlackListDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $uibModal,Notification ,entity, BlackList, Client,Nationalite) {
        var vm = this;

        vm.blackList = entity;
        console.log(entity);
        vm.clear = clear;
        vm.save = save;
        vm.filtre = false;
        vm.clients=[];
        vm.clientFinal=[];
        vm.req=[];
        vm.ctrlClient1=false;
        vm.ctrlClient2=false;
        vm.blackList.clientIdent=null;
        vm.clientBId= null;
        vm.annul=false;
        vm.listCtrl=true;
        // vm.clients = BlackList.queryAll( function (result) {
        //     vm.clients=result;
        //     console.log('new requete',vm.clients);
        // });
        vm.afficherFiltre = afficherFiltre;
        vm.annuler= annuler;
        vm.itemSearch = {
            numero:null,
            code: null,
            cliendId: null
        };
        vm.nationalites = Nationalite.query();
        // vm.personne = BlackList.queryAll( function (result) {
        // });
        console.log('ddd',vm.nationalite);
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });
        vm.clientColums = [{
            title: 'Nom',
            name: 'title'
        }];
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            vm.blackList.clientId=vm.clientBId;
            console.log(vm.blackList);
            if (vm.blackList.id !== null) {
                BlackList.update(vm.blackList, onSaveSuccess, onSaveError);
            } else {
                BlackList.save(vm.blackList, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:blackListUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        $scope.$watch("vm.clientIds", function (ids) {
            console.log('---start---', ids);
            vm.auth2 = [];
            console.log('---end---');
            if (angular.isDefined(ids)) {
                console.log('ggg');
                var tests;
                tests = _.find(vm.clientPList, {'id': ids});
                console.log('ggg',tests);
                vm.clientFinal= tests;
                vm.clientBId = tests.id
            }

        });

        function afficherFiltre() {
            vm.filtre = true;
            vm.annul=true;
            vm.listCtrl=false;
            console.log('code et numero',vm.itemSearch.code,vm.itemSearch.numero);
            BlackList.findClientBlackList({code:vm.itemSearch.code,numero:vm.itemSearch.numero},function (data) {
                console.log('par nationalité',data);
                vm.req = data;
                if(data){
                    var i=0;
                    vm.clientP=[];
                    vm.clientPList=[];
                    angular.forEach(data,function (value,index) {
                        var v = {};
                        v.name = value.nomClient;
                        v.prenom = value.prenomClient;
                        i++;
                        v.id = value.id;
                        v.title = value.nomClient+' '+value.prenomClient;
                        vm.clientPList.push(v);
                        vm.clientP.push(v);
                    });
                    console.log('OK result',vm.clientP)
                }

            });
       /*     if (vm.itemSearch.code && vm.itemSearch.numero === null){
                BlackList.findByCode({id: vm.itemSearch.code}, function (data) {
                    console.log('par nationalité',data);
                    vm.req = data;
                    //vm.clientPList= data;
                    if(data){
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
            if (vm.itemSearch.numero && vm.itemSearch.code === null ){
                BlackList.findByTel({id: vm.itemSearch.numero}, function (data) {
                    console.log('par telephone',data);
                    vm.clients = data;
                    vm.req = data;
                    if(data){
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
                        console.log('b',vm.clientP)
                    }

                })
            }
            if (vm.itemSearch.numero && vm.itemSearch.code){
                BlackList.findByCodeAndTel({id1: vm.itemSearch.code , id2: vm.itemSearch.numero }, function (data) {
                    console.log('par telephone et code',data);
                    vm.clients = data;
                    vm.req = data;
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
                            console.log(vm.clientP)
                        });
                        console.log(vm.clientP)

                })
            }*/
        }
        function annuler() {
            vm.itemSearch = {
                'numero':null,
                'code': null,
                'cliendId': null
            };
            vm.ctrlClient1=false;
            vm.ctrlClient2=false;
            vm.annul=false;
            vm.clientP=[];
            vm.clientPList=[];
            vm.blackList = null;
            // vm.clients = BlackList.queryAll( function (result) {
            //     //vm.clients=result;
            // });
        }


    }
})();
