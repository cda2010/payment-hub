(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BlackListController', BlackListController);

    BlackListController.$inject = ['$state', 'BlackList', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams','$scope','$uibModal','Nationalite','TableService'];

    function BlackListController($state, BlackList, ParseLinks, AlertService, paginationConstants, pagingParams,$scope,$uibModal,Nationalite,TableService) {

        var vm = this;
        vm.blackLists = [];
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
       vm.clientDto= [];
       vm.retirer = retirer;
        vm.afficheFiltreB=false;
        vm.afficherFiltreB=afficherFiltreB;
       vm.chercherB=chercherB;
        vm.detailsOperation=detailsOperation;
       vm.nationalite = Nationalite.query();
        vm.canShowResultCount=false;
        vm.totalItems = 50000;
        //loadAll();
        init();
        //attributs de filtrage
        vm.itemSearch = {
            'nomClient':null,
            'prenomClient':null,
            'typePersonne':null,
            'telephone':null,
            'nomMereClient': null,
            'actionBlackList': null
        };
        vm.allData=[];
        function loadAll () {
            console.log('get All');
            BlackList.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.blackLists = data;
                vm.page = pagingParams.page;
                console.log(vm.blackLists);
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }
        
        function retirer(id) {
            console.log('id',id);
            BlackList.delate({id:id});
            init();
        }
        function afficherFiltreB(){
            vm.afficheFiltreB = !vm.afficheFiltreB;
            init();
            /*if (vm.allData.length<=0)
            {
                vm.allData=  BlackList.query();
            }
            if(vm.afficheFiltreB)
            {
                vm.blackLists = BlackList.query();
                vm.itemSearch = {
                    'nomClient':null,
                    'prenomClient':null,
                    'typePersonne':null,
                    'telephone':null,
                    'nomMereClient': null,
                    'actionBlackList': null
                };
            }*/
        }
        function chercherB() {
            vm.blackLists = filterB();
        }

        function filterB() {
            init();
            /*var resultat=[];
            for (let i = 0;i<vm.allData.length;i++)
            {
                if(!vm.allData[i].actionBlackList){
                    vm.allData[i].actionBlackList = " ";
                }

                if((!vm.itemSearch.nomMereClient || vm.allData[i].nomMereClient === vm.itemSearch.nomMereClient) &&
                    (!vm.itemSearch.nomClient || vm.allData[i].nomClient === vm.itemSearch.nomClient)&&
                    (! vm.itemSearch.prenomClient || vm.allData[i].prenomClient === vm.itemSearch.prenomClient) &&
                    (! vm.itemSearch.telephone || vm.allData[i].telephone === vm.itemSearch.telephone) &&
                    (! vm.itemSearch.actionBlackList || vm.allData[i].actionBlackList.includes(vm.itemSearch.actionBlackList)))
                {
                    resultat.push(vm.allData[i]);
                }
            }
            return resultat;*/
        }

        function detailsOperation(blacklist){
            console.log('data',blacklist);
            $uibModal.open({
                templateUrl: 'app/entities/operation-suivis/operation-suivis-client.html',
                controller: 'OperationSuivisClientController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return blacklist;
                    },
                    isViewing: true,
                }
            });
        }

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    'nomClient':null,
                    'prenomClient':null,
                    'typePersonne':null,
                    'telephone':null,
                    'nomMereClient': null,
                    'actionBlackList': null
                };
                init();
            }
        };
        vm.filtre = function () {
            console.log("filtre des informations en cours");
            vm.filtreCtrl=true;
            init();
        };

        function init() {
            console.log('get init');
            var addItemData = {};

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {};

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };


            var entityParams = function () {
                return {
                    "telephone": vm.itemSearch.telephone,
                    "nomClient": vm.itemSearch.nom,
                    "prenomClient": vm.itemSearch.prenom,
                    "nomMereClient": vm.itemSearch.adresse,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: BlackList,
                entitySearch: BlackList,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount = true;
        }
    }
})();
