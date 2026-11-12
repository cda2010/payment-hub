(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('EtiquetteMethodeDialogController', EtiquetteMethodeDialogController  );

    EtiquetteMethodeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EtiquetteMethode'];

    function EtiquetteMethodeDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, EtiquetteMethode) {
        var vm = this;

        vm.etiquetteMethode = entity;
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
            if (vm.etiquetteMethode.id !== null) {
                EtiquetteMethode.update(vm.etiquetteMethode, onSaveSuccess, onSaveError);
            } else {
                EtiquetteMethode.save(vm.etiquetteMethode, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:etiquetteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
