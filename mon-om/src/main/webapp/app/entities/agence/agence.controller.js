(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('AgenceController', AgenceController);

    AgenceController.$inject = ['$state', 'Agence', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams','$stateParams','fournisseur'];

    function AgenceController($state, Agence, ParseLinks, AlertService, paginationConstants, pagingParams,$stateParams,fournisseur) {

        var vm = this;
        vm.fournisseur= fournisseur;
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;

        loadAll();

        function loadAll () {
            Agence.queryFournisseur({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort(),
                fseId:pagingParams.fseId
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
                vm.agences = data;
                vm.page = pagingParams.page;
                vm.fseId=pagingParams.fseId;
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
                fseId:   vm.fseId,
                idFournisseur:vm.fseId,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch

            });
        }
    }
})();
