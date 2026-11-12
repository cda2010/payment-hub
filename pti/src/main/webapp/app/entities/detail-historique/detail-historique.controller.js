(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailHistoriqueController', DetailHistoriqueController);

    DetailHistoriqueController.$inject = ['$state','TableService','$scope' ,'DetailHistorique', 'ParseLinks', 'AlertService', 'DateUtils','Utils','BASE_URL_API','paginationConstants', 'pagingParams'];

    function DetailHistoriqueController($state,TableService,$scope ,DetailHistorique, ParseLinks, AlertService, DateUtils,Utils,BASE_URL_API,paginationConstants, pagingParams) {

        var vm = this;
        var URL=BASE_URL_API + "etat/export/historiqueSolde";
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.chercherHistorique=chercherHistorique;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.canShowResultCount=false;
        vm.selected=false;
        vm.dateEnregistrement=null;
        vm.generateAll=generateAll;
        vm.format=null;
        vm.itemSearch= {
          "typeHistorique":null,
          "dateEnregistrement":null,
            "telephone": null,
            "format":null
        };

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }




        function loadAll () {
            vm.dateEnregistrement=DateUtils.convertLocalDateToServer(vm.itemSearch.dateEnregistrement);
            console.log(vm.itemSearch);
            DetailHistorique.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                typeHistorique:vm.itemSearch.typeHistorique,
                dateEnregistrement:vm.itemSearch.dateEnregistrement,
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
                vm.detailHistoriques = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function init() {

            vm.itemSearch.dateEnregistrement=DateUtils.convertLocalDateToServer(vm.dateEnregistrement);

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
                    "typeHistorique":vm.itemSearch.typeHistorique,
                    "dateEnregistrement": vm.itemSearch.dateEnregistrement,
                    "telephone": vm.itemSearch.telephone
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: DetailHistorique,
                entitySearch:DetailHistorique,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount=true;
        }

         function chercherHistorique ()  {
             vm.selected=true;
            console.log("filtre des informations en cours");
            // vm.itemSearch.dateEnregistrement=DateUtils.convertLocalDateToServer(vm.itemSearch.dateEnregistrement);
             init();
            //loadAll();
        };

        function generateAll() {
            vm.itemSearch.dateEnregistrement=DateUtils.convertLocalDateToServer(vm.dateEnregistrement);
            vm.itemSearch.format = vm.format;
            console.log(vm.itemSearch,URL);

            if (vm.format === null || vm.format==='') {
                vm.itemSearch.format = 'PDF';
            }
            if (vm.itemSearch.format === 'PDF') {
                Utils.postPrintUrl(URL, vm.itemSearch);
            } else {
                if (vm.itemSearch.format === 'XLSX'&&vm.itemSearch.typeHistorique==='SOLDECLIENT') {
                    vm.name = 'HistoriqueSoldeClient.xlsx';
                }
                if (vm.itemSearch.format === 'CSV'&&vm.itemSearch.typeHistorique==='SOLDECLIENT') {
                    vm.name = 'HistoriqueSoldeClient.csv';
                }
                if (vm.itemSearch.format === 'XLSX'&&vm.itemSearch.typeHistorique==='SOLDEAGENT'){
                    vm.name = 'HistoriqueSoldeAgent.xlsx';
                }
                if (vm.itemSearch.format === 'CSV'&&vm.itemSearch.typeHistorique==='SOLDEAGENT') {
                    vm.name = 'HistoriqueSoldeAgent.csv';
                }
                if (vm.itemSearch.format === 'XLSX'&&vm.itemSearch.typeHistorique==='TABLEAUSOLDE') {
                    vm.name = 'HistoriqueTableauSolde.xlsx';
                }
                if (vm.itemSearch.format === 'CSV'&&vm.itemSearch.typeHistorique==='TABLEAUSOLDE') {
                    vm.name = 'HistoriqueTableauSolde.csv';
                }
                Utils.downloadFile(URL, vm.itemSearch, vm.name);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            vm.itemSearch.dateEnregistrement=DateUtils.convertLocalDateToServer(vm.dateEnregistrement);
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch,
                typeHistorique:vm.itemSearch.typeHistorique,
                dateEnregistrement:vm.itemSearch.dateEnregistrement,
                telephone: vm.itemSearch.telephone
            });
        }
    }
})();
