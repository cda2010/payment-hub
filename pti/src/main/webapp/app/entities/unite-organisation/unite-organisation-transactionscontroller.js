(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationTransactionsController', UniteOrganisationTransactionsController);

    UniteOrganisationTransactionsController.$inject = ['$scope', '$rootScope', 'PopupService', '$uibModal', '$uibModalInstance','Utils' ,'entity','BASE_URL_API' ,'TransactionComptePti'];

    function UniteOrganisationTransactionsController($scope, $rootScope, PopupService, $uibModal, $uibModalInstance,Utils ,entity,BASE_URL_API ,TransactionComptePti) {

        var vm = this;
        var URL = BASE_URL_API +'etat/export/transactionOfCompteAutre';
        vm.compte = entity;
        vm.transactions = [];
        vm.totalItems = 0;
        vm.queryCount = 0;
        vm.page = 1;
        vm.format=null;
        vm.itemsPerPage=20;
        vm.exporterComm=exporterComm;
        vm.transactionDate=transactionDate;
        // console.log(vm.totalItems);
        vm.canShowResultCount = false;
        vm.itemSearch = {
            'compteAutreId':null,
            'format':null,
            "dateDebut": null,
            "dateFin": null,
            'type': null
        };
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }
        vm.setItemsPerPage=function(nombre){
            vm.itemsPerPage=nombre;
            getDetailTransaction();
        };
        vm.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        vm.initFiltre = function () {
            vm.itemSearch = {
                "dateDebut": null,
                "dateFin": null,
            };
        };
        function transactionDate() {
            getDetailTransaction();
        }
        getDetailTransaction();

        function getDetailTransaction() {
            console.log('f',vm.itemSearch);
            TransactionComptePti.queryByCompteAutreOptimise({
                compteAutreId: vm.compte.compteAutreId,
                dateDebut: vm.itemSearch.dateDebut,
                dateFin: vm.itemSearch.dateFin,
                page: vm.page - 1,
                size: vm.itemsPerPage
            }, function (result, headers) {
                //console.log(result);
                vm.transactions = result;
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = result.length;
                console.log(vm.totalItems,vm.queryCount);
                console.log('transaction',result);
                vm.canShowResultCount = true;
            })
        }
        function exporterComm() {
            console.log('yes bro');
            vm.itemSearch.compteAutreId=vm.compte.compteAutreId;
            vm.itemSearch.format=vm.format;
            if(vm.format===null || vm.format===''){
                console.log('f',vm.format);
                vm.itemSearch.format = 'PDF';
            }
            if(vm.itemSearch.format ==='PDF' || vm.itemSearch.format ===null)
            {
                Utils.postPrintUrl(URL,vm.itemSearch);
            } else {
                if(vm.itemSearch.format ==='XLSX') {console.log('oui') ; vm.name='ExportuniteTransactionCptAutre.xlsx';}
                if(vm.itemSearch.format ==='CSV') {console.log('oui') ;vm.name='ExportuniteTransactionCptAutre.csv';}
                Utils.downloadFile(URL,vm.itemSearch,vm.name);
            }
        };
        $scope.$watch("vm.page", function (id) {
            getDetailTransaction();
        });
    }
})();
