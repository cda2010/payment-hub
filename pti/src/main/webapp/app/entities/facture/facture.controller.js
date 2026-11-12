(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FactureController', FactureController);

    FactureController.$inject = ['$state', 'BASE_URL_API' ,'TableService','DateUtils','$scope','Facture','Produit','FactureType','Utils'];

    function FactureController($state,BASE_URL_API ,TableService,DateUtils, $scope,Facture,Produit,FactureType,Utils) {

        var vm = this;
        var URL=  BASE_URL_API + "etat/factures/listing";
        vm.factures = [];
        vm.filtrer=true;
        vm.canShowResultCount=false;
        vm.format=null;
        vm.exporterFacture=exporterFacture;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.produits=Produit.query();
        vm.export=false;
        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }
     /*   Facture.query(function(result) {
            console.log(result);
            vm.totalItems=result.length+1;
            vm.factures = result;
            vm.searchQuery = null;
        });*/
       // loadAll();

        function loadAll() {
            Facture.query(function(result) {
                vm.factures = result;
                vm.searchQuery = null;
            });
        }
        vm.itemSearch = {
            "refFacture":null,
            "refPaiement":null,
            "produit":null,
            "datePaiement":null,
            "format": null,
            "facturierId": null
        };
        function exporterFacture() {
            vm.itemSearch.format=vm.format;
            if(vm.format===null || vm.format===''){
                console.log('f',vm.format);
                vm.itemSearch.format = 'PDF';
            }
            if(vm.itemSearch.datePaiement!=null){
                console.log(vm.itemSearch.datePaiement);
                vm.itemSearch.datePaiement=DateUtils.convertLocalDateToServer(vm.itemSearch.datePaiement);
            }
            console.log('top',vm.itemSearch);
            if(vm.itemSearch.format ==='PDF' || vm.format ===null)
            {
                Utils.postPrintUrl(URL,vm.itemSearch);
            } else {
                if(vm.itemSearch.format ==='XLSX') {vm.name='ExportFacture.xlsx';}
                if(vm.itemSearch.format ==='CSV') {vm.name='ExportFacture.csv';}
                Utils.downloadFile(URL,vm.itemSearch,vm.name);
            }
        }
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
                    "refFacture":vm.itemSearch.refFacture,
                    "refPaiement":vm.itemSearch.refPaiement,
                    "produit":vm.itemSearch.produit,
                    "datePaiement":vm.itemSearch.datePaiement
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: FactureType,
                entitySearch:FactureType,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            console.log(datas);
            TableService.init(datas);
            vm.canShowResultCount=true;
        }
        vm.filtre = function () {
            console.log("filtre des informations en cours");
            if(vm.itemSearch.datePaiement!=null){
                console.log(vm.itemSearch.datePaiement);
                var dateString=DateUtils.convertLocalDateToServer(vm.itemSearch.datePaiement);
                vm.itemSearch.datePaiement=dateString;
                console.log(vm.itemSearch.datePaiement,'et',dateString);
            }
            console.log(vm.itemSearch.refFacture,vm.itemSearch.refPaiement,vm.itemSearch.produit,vm.itemSearch.datePaiement);
            init();
            vm.export=true;
        };
    }
})();
