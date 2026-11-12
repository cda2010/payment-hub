(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InstructionComptableDialogController', InstructionComptableDialogController);

    InstructionComptableDialogController.$inject = ['RequestUtils','EtiquetteCompte','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InstructionComptable', 'isViewing'];

    function InstructionComptableDialogController (RequestUtils,EtiquetteCompte,$timeout, $scope, $stateParams, $uibModalInstance, entity, InstructionComptable, isViewing) {
        var vm = this;

        vm.instructionComptable = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;
        vm.typeservices = RequestUtils.TypeServiceList.query();
        vm.etiquetteComptes = RequestUtils.EtiquetteCompteList.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.instructionComptable.id !== null) {
                InstructionComptable.update(vm.instructionComptable, onSaveSuccess, onSaveError);
            } else {
                InstructionComptable.save(vm.instructionComptable, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:instructionComptableUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
