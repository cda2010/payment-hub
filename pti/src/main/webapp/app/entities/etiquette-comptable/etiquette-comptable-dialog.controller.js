(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteComptableDialogController', EtiquetteComptableDialogController);

    EtiquetteComptableDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EtiquetteComptable'];

    function EtiquetteComptableDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EtiquetteComptable) {
        var vm = this;

        vm.etiquetteComptable = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.etiquetteComptable.id !== null) {
                EtiquetteComptable.update(vm.etiquetteComptable, onSaveSuccess, onSaveError);
            } else {
                EtiquetteComptable.save(vm.etiquetteComptable, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:etiquetteComptableUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
