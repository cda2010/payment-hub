(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneOperationDialogController', LigneOperationDialogController);

    LigneOperationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneOperation', 'Operation'];

    function LigneOperationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneOperation, Operation) {
        var vm = this;

        vm.ligneOperation = entity;
        vm.clear = clear;
        vm.save = save;
        vm.operations = Operation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.ligneOperation.id !== null) {
                LigneOperation.update(vm.ligneOperation, onSaveSuccess, onSaveError);
            } else {
                LigneOperation.save(vm.ligneOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:ligneOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
