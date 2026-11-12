(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeServiceController', TypeServiceController);

    TypeServiceController.$inject = ['TypeService','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TypeServiceController(TypeService,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.typeServices = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;

        loadAll();

        function loadAll() {
            // TypeService.query(function(result) {
            //     vm.typeServices = result;
            //     vm.searchQuery = null;
            // });
            TypeService.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'codeTypeService') {
                    result.push('codeTypeService');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.typeServices = data;
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
