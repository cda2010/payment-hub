(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComptesController', ComptesController);

    ComptesController.$inject = ['$state', 'Comptes', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function ComptesController($state, Comptes, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.filter = filter;
        vm.filterStatus = false;

        loadAll();

        function filter(){
            vm.filterStatus = !vm.filterStatus;
            if (vm.filterStatus === false)
                Comptes.filtredWithCompteBank({
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            else
                Comptes.filtredWithCompteOther({
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
        }

        function loadAll () {
            Comptes.query({
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
            vm.totalItems = headers('X-Total-Count');
            vm.queryCount = vm.totalItems;
            vm.comptes = data;
            vm.page = pagingParams.page;
        }

        function onError(error) {
            AlertService.error(error.data.message);
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
