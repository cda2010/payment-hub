(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeCompteController', TypeCompteController);

    TypeCompteController.$inject = ['TypeCompte','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TypeCompteController(TypeCompte,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.typeComptes = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;

        loadAll();

        function loadAll() {
            // TypeCompte.query(function(result) {
            //     vm.typeComptes = result;
            //     vm.searchQuery = null;
            // });
            TypeCompte.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'codeParametre') {
                    result.push('codeParametre');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.typeComptes = data;
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
