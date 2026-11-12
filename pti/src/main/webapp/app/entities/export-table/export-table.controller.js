(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ExportTableController', ExportTableController);

    ExportTableController.$inject = ['$state', 'ExportTable', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', '$uibModal'];

    function ExportTableController($state, ExportTable, ParseLinks, AlertService, paginationConstants, pagingParams, $uibModal) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.viewExportTable=viewExportTable;
        
        loadAll();

        function loadAll () {
            ExportTable.query({
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
                console.log('vm.queryCount',vm.queryCount);
                vm.exportTables = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function viewExportTable(idExport) {
            $uibModal.open({
                templateUrl: 'app/entities/export-table/export-table-detail.html',
                controller: 'ExportTableDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: ['ExportTable', function(ExportTable) {
                        return ExportTable.get({id : idExport}).$promise;
                    }]
                }
            }).result.then(function() {
            }, function() {
            });

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
