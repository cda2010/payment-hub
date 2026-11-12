(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('SchemaEcritureController', SchemaEcritureController);

    SchemaEcritureController.$inject = ['SchemaEcriture', 'TypeService', 'pagingParams', 'ParseLinks'];

    function SchemaEcritureController(SchemaEcriture, TypeService, pagingParams, ParseLinks) {
        var vm = this;
        
        // Existing properties
        vm.changeService = changeService;
        vm.schemaEcritures = [];
        vm.typeServices = TypeService.query();
        
        // New pagination properties
        vm.itemsPerPage = 20;
        vm.page = 1;
        vm.totalItems = null;
        vm.transition = transition;
        
        loadAll();

        function changeService(typeService) {
            if (typeService !== null && typeService !== undefined) {
                SchemaEcriture.queryService({
                    typeService: typeService,
                    page: vm.page - 1,
                    size: vm.itemsPerPage
                }, onSuccess, onError);
                pagingParams.typeService = typeService;
            } else {
                pagingParams.typeService = null;
                loadAll();
            }
        }

        function loadAll() {
            if (angular.isDefined(pagingParams.typeService) && pagingParams.typeService !== null) {
                vm.typeService = {id: pagingParams.typeService};
                SchemaEcriture.queryService({
                    typeService: pagingParams.typeService,
                    page: vm.page - 1,
                    size: vm.itemsPerPage
                }, onSuccess, onError);
            } else {
                SchemaEcriture.query({
                    page: vm.page - 1,
                    size: vm.itemsPerPage
                }, onSuccess, onError);
            }
        }

        function onSuccess(data, headers) {
            vm.totalItems = headers('X-Total-Count');
            vm.queryCount = vm.totalItems;
            vm.schemaEcritures = data;
        }

        function onError(error) {
            console.error('Error loading schema ecritures:', error);
        }

        function transition() {
            loadAll();
        }
    }
})();
