(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LienController', LienController);

    LienController.$inject = ['$state', 'Lien', 'ParseLinks', 'AlertService','$uibModal' ,'paginationConstants', 'pagingParams'];

    function LienController($state, Lien, ParseLinks, AlertService, $uibModal,paginationConstants, pagingParams) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.voirLiens=voirLiens;

        loadAll();
    
            
        function voirLiens(lienId) {
            console.log(lienId);
            $uibModal.open({
                templateUrl: 'app/entities/lien/lien-detail.html',
                controller: 'LienViewController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return lienId;
                    }
                }
            }).result.then(function () {
            }, function () {
            });
        }
        
        function loadAll () {
            Lien.query({
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
                vm.liens = data;
                vm.page = pagingParams.page;
                console.log(vm.liens);
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
