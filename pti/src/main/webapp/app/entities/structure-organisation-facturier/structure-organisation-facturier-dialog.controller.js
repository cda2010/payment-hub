(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('StructureOrganisationFacturierDialogController', StructureOrganisationFacturierDialogController);

    StructureOrganisationFacturierDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'StructureOrganisationFacturier','UniteOrganisation'];

    function StructureOrganisationFacturierDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, StructureOrganisationFacturier,UniteOrganisation) {
        var vm = this;

        vm.structureOrganisationFacturier = entity;
        vm.clear = clear;
        vm.save = save;
        vm.uniteOrganisations=UniteOrganisation.queryByTypeUoFSE();
        console.log(vm.uniteOrganisations);

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.structureOrganisationFacturier.id !== null) {
                StructureOrganisationFacturier.update(vm.structureOrganisationFacturier, onSaveSuccess, onSaveError);
            } else {
                StructureOrganisationFacturier.save(vm.structureOrganisationFacturier, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:structureOrganisationFacturierUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
