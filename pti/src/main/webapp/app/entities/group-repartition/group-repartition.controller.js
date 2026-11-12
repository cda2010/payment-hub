(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupRepartitionController', GroupRepartitionController);

    GroupRepartitionController.$inject = ['$state','$uibModal','Notification','GroupRepartition','RequestUtils', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams','$scope','TableService','TableauRepartitionCommission','LigneRepartitionCommissionByTableau','TypeUniteOrganisation'];

    function GroupRepartitionController($state,$uibModal,Notification, GroupRepartition,RequestUtils, ParseLinks, AlertService, paginationConstants, pagingParams,$scope,TableService,TableauRepartitionCommission,LigneRepartitionCommissionByTableau,TypeUniteOrganisation) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.init = init;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.canShowResultCount=false;
        vm.tableauRepartitions=null;
        vm.ligneRepartitions=null;
        vm.typeUOs=null;
        vm.curentGroup=null;
        vm.curentTableauRepartition=null;
        vm.totalItems=1000;
            TypeUniteOrganisation.queryOptimisetUo(function (data) {
                vm.typeUOs=data;
            });
        vm.findTableauRepartitions=function(group){
            vm.curentGroup=group;
            TableauRepartitionCommission.queryByGroupOptimise({groupId:group.id},function (result) {
                angular.forEach(result, function (value, key) {
                    var found = _.find(vm.typeUOs, { 'niveau': value.niveaoOperation });
                    if (!angular.isUndefined(found)) {
                        value.niveau=found.libelleTypeUO;
                    }
                });
                vm.tableauRepartitions=result;
            })

        };
        if(vm.curentTableauRepartition!=null){
            LigneRepartitionCommissionByTableau.query({id:vm.curentTableauRepartition.id},function (result) {
                vm.ligneRepartitions=result;
                vm.totalItems= result.length + 1;
            });
        }
        init(vm.curentTableauRepartition);
        function init(tableau) {
            console.log('Total item 1',vm.totalItems);
            if(vm.ligneRepartitions!=null){
                vm.totalItems= vm.ligneRepartitions.length + 1;
            }
            if(tableau!=null){
                console.log('1111111');
            var deleteItemData = {
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-delete-dialog.html',
                controller: 'LigneRepartitionCommissionDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };
            var addItemData = {
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-dialog.html',
                controller: 'LigneRepartitionCommissionDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function() {
                        return {

                        };
                    }
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: {},
                deleteData: deleteItemData
            };

            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                    "id": tableau.id
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                //entity: LigneRepartitionCommissionByTableau,
                entity: RequestUtils.LigneRepartitionCommissionOptimise,
                //entitySearch: LigneRepartitionCommissionByTableau,
                entitySearch: RequestUtils.LigneRepartitionCommissionOptimise,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount = true;
            console.log('find',datas);
            }
        }

        vm.addTableauRepartition=function(){
            $uibModal.open({
                templateUrl: 'app/entities/tableau-repartition-commission/tableau-repartition-commission-dialog.html',
                controller: 'TableauRepartitionCommissionDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            "id" : null,
                            "niveaoOperation" : null,
                            "dateDebut" : null,
                            "dateFin" : null,
                            "typeServiceId" : vm.curentGroup.typeServiceId,
                            "typeServiceLibelle" : null,
                            "organisationId" : null,
                            "organisationLibelle" : null,
                            "activated" : true,
                            "groupId" : vm.curentGroup.id,
                            "groupLibelle" : null,
                            'disableGroupField':true,
                        }
                    }
                }
            }).result.then(function() {
                vm.findTableauRepartitions(vm.curentGroup);
                Notification.success("Tableau ajouté avec succès");
            }, function() {
            });
        };
        vm.editTableauRepartition=function(tableau){
            $uibModal.open({
                templateUrl: 'app/entities/tableau-repartition-commission/tableau-repartition-commission-dialog.html',
                controller: 'TableauRepartitionCommissionDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return tableau;
                    }
                }
            }).result.then(function() {
                vm.findTableauRepartitions(vm.curentGroup);
                Notification.success("Tableau modifié avec succès");
            }, function() {
            });
        }
        vm.deleteTableauRepartition=function(tableau){
            $uibModal.open({
                templateUrl: 'app/entities/tableau-repartition-commission/tableau-repartition-commission-delete-dialog.html',
                controller: 'TableauRepartitionCommissionDeleteController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return tableau;
                    }
                }
            }).result.then(function() {
                vm.findTableauRepartitions(vm.curentGroup);
                Notification.success("Tableau modifié avec succès");
            }, function() {
            });
        }
        vm.findLigneRepartitions=function(tableau){
            vm.curentTableauRepartition=tableau;
            LigneRepartitionCommissionByTableau.queryOptimise({id:tableau.id},function (result) {
             /*   angular.forEach(result, function (value, key) {
                    var found = _.find(vm.typeUOs, { 'niveau': value.niveau });
                    if (!angular.isUndefined(found)) {
                        value.niveauLibelle=found.libelleTypeUO;
                    }
                });*/
                console.log('ok',result);
                vm.ligneRepartitions=result;
                vm.totalItems= result.length + 1;
            });
            console.log('Total item',tableau.id,vm.totalItems);
            console.log("------Start-------");
            init(tableau)
        };
        vm.addLigneRepartition=function(){
            $uibModal.open({
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-dialog.html',
                controller: 'LigneRepartitionCommissionDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            "id" : null,
                            "niveau" :null,
                            "valeur" : null,
                            "surFrais" : true,
                            "typeTarif" : 'POURCENTAGE',
                            "tableauRepartitionCommissionId" : vm.curentTableauRepartition.id,
                            "disableTableauRepartitionCommissionField":true,
                        }
                    }
                }
            }).result.then(function() {
                vm.findLigneRepartitions(vm.curentTableauRepartition);
                Notification.success("Ligne repartition ajouté avec succès");
            }, function() {
            });
        };
        vm.editLigneRepartition=function(ligne){
            $uibModal.open({
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-dialog.html',
                controller: 'LigneRepartitionCommissionDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return ligne;
                    }
                }
            }).result.then(function() {
                vm.findLigneRepartitions(vm.curentTableauRepartition);
                Notification.success("Ligne repartition modifié avec succès");
            }, function() {
            });
        };
        vm.deleteLigneRepartition=function(ligne){
            $uibModal.open({
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-delete-dialog.html',
                controller: 'LigneRepartitionCommissionDeleteController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return ligne;
                    }
                }
            }).result.then(function() {
                vm.findLigneRepartitions(vm.curentTableauRepartition);
                Notification.success("Ligne repartition modifié avec succès");
            }, function() {
            });
        };

        loadAll();

        function loadAll () {
            if(vm.ligneRepartitions==null){
            GroupRepartition.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            }
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                if(vm.ligneRepartitions==null){
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.groupRepartitions = data;
                vm.page = pagingParams.page;
                }
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            if(vm.ligneRepartitions==null){
                vm.page = page;
                vm.transition();
            }
        }

        function transition() {
            if(vm.ligneRepartitions==null){
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
            }
        }

    }
})();
