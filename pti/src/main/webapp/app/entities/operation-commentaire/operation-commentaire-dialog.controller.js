(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationCommentaireDialogController', OperationCommentaireDialogController);

    OperationCommentaireDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'OperationCommentaire', 'Operation'];

    function OperationCommentaireDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, OperationCommentaire, Operation) {
        var vm = this;

        vm.operationCommentaire = entity;
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
            if (vm.operationCommentaire.id !== null) {
                OperationCommentaire.update(vm.operationCommentaire, onSaveSuccess, onSaveError);
            } else {
                OperationCommentaire.save(vm.operationCommentaire, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:operationCommentaireUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
