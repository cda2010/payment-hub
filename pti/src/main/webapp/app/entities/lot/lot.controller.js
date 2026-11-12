/* eslint-disable no-unused-vars */
/* eslint-disable no-console */
/* eslint-disable angular/log */
/* eslint-disable no-undef */
(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LotController', LotController);

    LotController.$inject = ['$state','Lot','pagingParams','ParseLinks','paginationConstants','$uibModal'];

    function LotController($state,Lot,pagingParams,ParseLinks,paginationConstants,$uibModal) {

        var vm = this;
        vm.lots = [];
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.viewLot=viewLot;
        vm.setActive=setActive;


        function setActive(lot, estActif) {
            lot.etat = estActif;
            var info;
            if(estActif){
                info= 'Activé';
            } else {
                info= 'Desactivé';
            }
            Lot.setEtat(lot, function (result) {
                console.log('updated',result);
                
            }, function (result) {
                console.log('echec',result);
                lot.etat =!lot.etat;
            });
        }

        function viewLot(idLot) {
            $uibModal.open({
                templateUrl: 'app/entities/lot/lot-detail.html',
                controller: 'LotDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: ['Lot', function(Lot) {
                        return Lot.get({id : idLot}).$promise;
                    }]
                }
            }).result.then(function() {
            }, function() {
            });

        }

        loadAll();

        function loadAll() {
            Lot.query({
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
                vm.lots = data;
                console.log(vm.lots);
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.page = pagingParams.page;
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

    }
})();
