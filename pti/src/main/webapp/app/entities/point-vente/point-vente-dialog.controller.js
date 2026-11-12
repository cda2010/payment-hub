(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointVenteDialogController', PointVenteDialogController);

    PointVenteDialogController.$inject = ['TableService','PointVente','Notification','$timeout','isViewing','isSaveInUO', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'PointVenteByUO', 'Operation', 'UniteOrganisation'];

    function PointVenteDialogController (TableService,PointVente,Notification,$timeout,isViewing,isSaveInUO, $scope, $stateParams, $uibModalInstance, entity, PointVenteByUO, Operation, UniteOrganisation) {
        var vm = this;

        vm.pointVente = entity;
        vm.isViewing = isViewing;
        vm.isSaveInUO = isSaveInUO;
        vm.close = clear;
        vm.save = save;
        //vm.operations = Operation.query();
        vm.operations = null;
        vm.uniteorganisations = UniteOrganisation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.pointVente.id !== null) {
                PointVente.update(vm.pointVente, onSaveSuccess, onSaveError);
            } else {
                PointVente.save(vm.pointVente, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:pointVenteUpdate', result);
            if(!vm.isSaveInUO){
            $uibModalInstance.close(result);
            }else {
                Notification.success("enregistrement réussit");
                vm.loadAll();
            }
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        if(vm.isSaveInUO){
            var deleteItemData = {
                templateUrl: 'app/entities/point-vente/point-vente-delete-dialog.html',
                controller: 'PointVenteDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                deleteData: deleteItemData
            };
            var entitySearchParams = function() {
                return {

                };
            };
            var entityParams = function() {
                return {
                    uoId: vm.pointVente.uniteOrganisationId
                };
            };


            var datas = {
                scope: $scope,
                vm: vm,
                entity: PointVenteByUO,
                dialogData: dialogData,
                entitySearch: PointVenteByUO,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.setItemsPerPage(10);


        }

    }
})();
