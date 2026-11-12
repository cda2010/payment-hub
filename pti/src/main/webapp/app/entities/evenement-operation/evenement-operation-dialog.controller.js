(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementOperationDialogController', EvenementOperationDialogController);

    EvenementOperationDialogController.$inject = ['$timeout', '$scope', '$stateParams', 'isViewing', '$uibModalInstance', 'entity', 'EvenementOperation', 'Operation', 'Compte'];

    function EvenementOperationDialogController ($timeout, $scope, $stateParams, isViewing, $uibModalInstance, entity, EvenementOperation, Operation, Compte) {
        var vm = this;

        vm.evenementOperation = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;
        vm.operations = Operation.query();
        vm.comptes = Compte.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.evenementOperation.id !== null) {
                EvenementOperation.update(vm.evenementOperation, onSaveSuccess, onSaveError);
            } else {
                EvenementOperation.save(vm.evenementOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:evenementOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
