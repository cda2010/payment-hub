(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationSuivisDialogController', OperationSuivisDialogController);

    OperationSuivisDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'OperationSuivis', 'Client'];

    function OperationSuivisDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, OperationSuivis, Client) {
        var vm = this;

        vm.operationSuivis = entity;
        vm.clear = clear;
        vm.save = save;
        vm.clients = Client.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.operationSuivis.id !== null) {
                OperationSuivis.update(vm.operationSuivis, onSaveSuccess, onSaveError);
            } else {
                OperationSuivis.save(vm.operationSuivis, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:operationSuivisUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
