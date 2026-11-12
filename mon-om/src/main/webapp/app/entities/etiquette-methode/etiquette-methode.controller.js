(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('EtiquetteMethodeController', EtiquetteMethodeController);


    EtiquetteMethodeController.$inject = ['EtiquetteMethode','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function EtiquetteMethodeController(EtiquetteMethode,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.etiquettesMethodes = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;
        loadAll();

        function loadAll() {
            // EtiquetteMethode.query(function(result) {
            //     vm.etiquettesMethodes = result;
            //     vm.searchQuery = null;
            // });
            EtiquetteMethode.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'code') {
                    result.push('code');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.etiquettesMethodes = data;
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
