(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('FournisseurServiceController', FournisseurServiceController);

    FournisseurServiceController.$inject = ['FournisseurService','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function FournisseurServiceController(FournisseurService,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.fournisseurServices = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;

        loadAll();

        function loadAll() {
            // FournisseurService.query(function(result) {
            //     vm.fournisseurServices = result;
            //     vm.searchQuery = null;
            // });
            FournisseurService.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'codeFse') {
                    result.push('codeFse');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.fournisseurServices = data;
                vm.page = pagingParams.page;
                vm.searchQuery = null;
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
