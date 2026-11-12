(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationSuivisClientController', OperationSuivisClientController);

    OperationSuivisClientController.$inject = ['$state', 'TableService','$scope','OperationSuivis','entity',  'Utils','BASE_URL_API','AlertService', 'Nationalite', '$uibModalInstance','Client', 'Notification'];

    function OperationSuivisClientController($state, TableService,$scope,OperationSuivis,entity,  Utils,BASE_URL_API,AlertService, Nationalite, $uibModalInstance,Client, Notification) {

        var vm = this;
        vm.tableau = false;
        var URL = BASE_URL_API + "etat/";
        vm.url= URL+'export/opearion-suivis-client';
        vm.blacklist=entity;
        vm.afficheFiltreO = false;
        vm.exportOperationSuivie=exportOperationSuivie;
        vm.fermerFenetre=fermerFenetre;
        vm.nationalites = Nationalite.query();
        vm.canShowResultCount=false;
        vm.format=null;
        vm.clients=[];
        vm.totalItems=100000;
        vm.clientColums = [{
            title: 'Nom',
            name: 'title'
        }];
        init();

        vm.itemSearch = {
            'national_id':null,
            'pnomCli':null,
            'tel':null
        };
        vm.item = {
            format: null,
            idClient:null
        };
        function fermerFenetre () {
            console.log('cancled');
            $uibModalInstance.dismiss('cancel');
        }


        function  exportOperationSuivie() {
            vm.item.idClient=vm.blacklist.clientId;
            if(vm.format===null || vm.format===''){
                console.log('f',vm.format);
                console.log('item',vm.item);
                vm.item.format = 'PDF';
            }
            if(vm.item.format ==='PDF' || vm.item.format ===null)
            {
                Utils.postPrintUrl(vm.url,vm.item);
            } else {
                if(vm.typeP==='PHYSIQUE') {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportClientPhysique.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportClientPhysique.csv';
                    }
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportClientMorale.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportClientMorale.csv';
                    }
                }
                Utils.downloadFile(vm.url,vm.item,vm.name);
            }

        }
        function init() {
            console.log('get init',vm.blacklist);
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
                    "id": vm.blacklist.clientId,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: OperationSuivis,
                entitySearch: OperationSuivis,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            console.log('data',datas);
            vm.canShowResultCount = true;
        }

    }
})();
