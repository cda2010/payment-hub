/* eslint-disable angular/json-functions */
/* eslint-disable angular/di */
/* eslint-disable angular/controller-as */
/* eslint-disable no-console */
/* eslint-disable angular/log */
(function() {
    'use strict';
   
    angular
        .module('ptiApp')
        .controller('LotOrganisationDialogController', LotOrganisationDialogController);

    LotOrganisationDialogController.$inject = ['$timeout', 'ClientByLot', 'TableAgentService','RequestUtils', '$scope', '$uibModalInstance', 'entity', 'Lot', 'DetailsLot', 'UniteOrganisation','LigneFichier'];

    function LotOrganisationDialogController ($timeout, ClientByLot, TableAgentService,RequestUtils, $scope, $uibModalInstance, entity, Lot, DetailsLot, UniteOrganisation,LigneFichier) {
        var vm = this;
        vm.varArray = new Array();
        
        vm.lot = entity;
        vm.clearr = clear;
        vm.save = save;
        console.log('entity-----',entity)
        //vm.detailsLots = DetailsLot.query();
        vm.clearSelect=clearSelect;
        vm.init = init;
        vm.getOrganisationByType=getOrganisationByType;
        vm.getOrganisation = getOrganisation;
        vm.organisationId=null;

        vm.canShowResultCount=false;

        vm.size = 0;
        vm.telephone=null;
        vm.organisationId=null;

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
                   
                    "organisationId": vm.organisationId,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: RequestUtils.GetLigneFichierDetailByOrganisation,
                entitySearch: RequestUtils.GetLigneFichierDetailByOrganisation,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            console.log('DATA------',vm.datas);
            TableAgentService.init(datas);
            vm.datas = [];
            vm.canShowResultCount = true;
        }
        function getOrganisationByType(typeUo){
            console.log('typeUo----',typeUo);
            if(typeUo==='MARCHAND'){
                UniteOrganisation.queryByType({typeId:'AGENCE'},function (result) {
                    vm.organisations=result;
                    console.log('result----',result);
                })
            } else if (typeUo==='SOUS_DISTRIBUTEUR'){
                UniteOrganisation.queryByType({typeId:'MASTER'},function (result) {
                    vm.organisations=result;
                })
            }
        }
        function getOrganisation(id) {
            vm.ctrlTabC = true;
            vm.stckId = id;
            console.log('id',id);
            vm.organisationId=id;
            init();
/*            if(id!==null){
                LigneFichier.queryDetailByOrganisation({
                    organisationId: id,
                    page: vm.page - 1,
                    size: vm.itemsPerPage,
                }, function (result, headers) {
                    console.log('lignes', result);
                    vm.totalItems = headers('X-Total-Count');
                    vm.queryCount = result.length;
                    console.log('transaction', result);
                    vm.canShowResultCount = true;
                    vm.ligneChargements = result;
                });
            }*/

        }

        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });
        $scope.$watch("vm.page", function (id) {
            getOrganisation(vm.organisationId, vm.selectedRow);
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            
            vm.lot.ligneChargementFichiers = vm.listeF;
            console.log('LOT',vm.listeF);
            console.log('LOT',vm.lot);
            vm.lot.typeActeur='ORGANISATION';
            vm.isSaving = true;
            if (vm.lot.id !== null) {
                Lot.update(vm.lot, onSaveSuccess, onSaveError);
            } else {
                console.log('LOT ORG',vm.lot);
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
