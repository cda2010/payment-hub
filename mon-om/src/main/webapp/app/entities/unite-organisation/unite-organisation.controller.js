(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('UniteOrganisationController', UniteOrganisationController);

    UniteOrganisationController.$inject = ['$state', 'UniteOrganisation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function UniteOrganisationController($state, UniteOrganisation, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.uniteOrganisations = [];
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;

        loadAll();

        function loadAll () {
            UniteOrganisation.query({
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
                vm.uniteOrganisations = data;
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
