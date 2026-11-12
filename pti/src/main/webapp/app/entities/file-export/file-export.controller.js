(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FileExportController', FileExportController);

        FileExportController.$inject = ['$state', '$scope','FileExport','TableService', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', '$uibModal'];

    function FileExportController($state, $scope,FileExport,TableService, ParseLinks, AlertService, paginationConstants, pagingParams, $uibModal) {

        var vm = this;

/*        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;*/
        vm.downloadFile = downloadFile;
        vm.canShowResultCount=false;
        
        init();

/*        function loadAll () {
            
            FileExport.query({
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
                vm.fileExports = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }*/

        function init() {

            var addItemData = {};

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {};

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };


            var entityParams = function () {
                return {

                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                //entity: ClientByTypePersonne,
                entity: FileExport,
                //entitySearch: ClientByTypePersonne,
                entitySearch: FileExport,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount = true;
        }

        // function addLeadingZeros(n) {
        //     if (n <= 9) {
        //       return "0" + n;
        //     }
        //     return n
        //   }
        // let currentDatetime = new Date()
        // vm.formattedDate = currentDatetime.getFullYear() + "" + addLeadingZeros(currentDatetime.getMonth() + 1) + "" + addLeadingZeros(currentDatetime.getDate()) + "" + addLeadingZeros(currentDatetime.getHours()) + "" + addLeadingZeros(currentDatetime.getMinutes()) + "" + addLeadingZeros(currentDatetime.getSeconds())
        // console.log(vm.formattedDate);

        // function viewExportTable(idExport) {
        //     $uibModal.open({
        //         templateUrl: 'app/entities/export-table/export-table-detail.html',
        //         controller: 'ExportTableDetailController',
        //         controllerAs: 'vm',
        //         backdrop: 'static',
        //         size: 'lg',
        //         resolve: {
        //             entity: ['ExportTable', function(ExportTable) {
        //                 return ExportTable.get({id : idExport}).$promise;
        //             }]
        //         }
        //     }).result.then(function() {
        //     }, function() {
        //     });

        // }

/*        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }*/


        function downloadFile(id){
            FileExport.download({id:id});            
        }

        
    }
})();
