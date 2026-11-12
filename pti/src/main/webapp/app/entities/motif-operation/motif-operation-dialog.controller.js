(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MotifOperationDialogController', MotifOperationDialogController);

    MotifOperationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'MotifOperation'];

    function MotifOperationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, MotifOperation) {
        var vm = this;

        vm.motifOperation = entity;
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
            if (vm.motifOperation.id !== null) {
                MotifOperation.update(vm.motifOperation, onSaveSuccess, onSaveError);
            } else {
                MotifOperation.save(vm.motifOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:motifOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
