/* eslint-disable angular/json-functions */
/* eslint-disable angular/di */
/* eslint-disable angular/controller-as */
/* eslint-disable no-console */
/* eslint-disable angular/log */
(function() {
    'use strict';
   
    angular
        .module('ptiApp')
        .controller('LotDialogController', LotDialogController);

    LotDialogController.$inject = ['$timeout', 'ClientByLot', 'TableNewService', '$scope', '$uibModalInstance', 'entity', 'Lot', 'DetailsLot', 'UniteOrganisation'];

    function LotDialogController ($timeout, ClientByLot, TableNewService, $scope, $uibModalInstance, entity, Lot, DetailsLot, UniteOrganisation) {
        var vm = this;
        vm.varArray = new Array();
        
        vm.lot = entity;
        vm.clearr = clear;
        vm.save = save;
        console.log('entity-----',entity)
        //vm.detailsLots = DetailsLot.query();
        vm.clearSelect=clearSelect;
        vm.init = init;

        vm.canShowResultCount=false;

        vm.size = 0;
        vm.telephone=null;

        // $scope.selectAll = function() {
        //     angular.forEach(vm.datas, function(client) {
        //         client.isChecked = vm.selectedAll;
        //     });
        // };

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
                   
                    "telephone": vm.telephone,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: ClientByLot,
                entitySearch: ClientByLot,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableNewService.init(datas);
            vm.datas = [];

            vm.canShowResultCount = true;
        }


        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            
            vm.lot.clients = vm.listeClients;
            
            vm.isSaving = true;
            if (vm.lot.id !== null) {
                Lot.update(vm.lot, onSaveSuccess, onSaveError);
            } else {
                Lot.save(vm.lot, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        function clearSelect() {
            vm.itemSearch = {
                telephone: null
            };
        }

    }
})();
