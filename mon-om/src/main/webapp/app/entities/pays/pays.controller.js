(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('PaysController', PaysController);

    PaysController.$inject = ['Pays','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function PaysController(Pays,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.pays = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;

        loadAll();

        function loadAll() {
            // Pays.query(function(result) {
            //     vm.pays = result;
            //     vm.searchQuery = null;
            // });
            Pays.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'nomPays') {
                    result.push('nomPays');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.pays = data;
                vm.page = pagingParams.page;
                vm.apiId = pagingParams.apiId;
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
