(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ApiController', ApiController);

    ApiController.$inject = ['Api','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function ApiController(Api,$state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.apis = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;
        vm.nomApiFilter = '';
        vm.fournisseurFilter = '';
        vm.uniqueNomApis = [];
        vm.uniqueFournisseurs = [];
        vm.loadAll = loadAll;
        vm.loadFilters = loadFilters;
vm.setActive=setActive;
        loadAll();
        loadFilters();

        function loadFilters() {
            Api.query({
                page: 0,
                size: 1000, // Large size to get all records
                sort: ['nomApi,asc']
            }, function(data) {
                if (data && data.length > 0) {
                    vm.uniqueNomApis = [...new Set(data.map(api => api.nomApi))].filter(Boolean);
                    vm.uniqueFournisseurs = [...new Set(data.map(api => api.fournisseurService.nom))].filter(Boolean);
                }
            });
        }


   function setActive (api, isActivated) {
            api.actif = isActivated;
            Api.update(api, function () {
                vm.loadAll();
                vm.clear();
            });
        }
        function loadAll() {
            Api.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort(),
                nomApi: vm.nomApiFilter || null,
                fournisseur: vm.fournisseurFilter || null
            }, onSuccess, onError);
            
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'codeApi') {
                    result.push('codeApi');
                }
                return result;
            }
            
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.apis = data;
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
