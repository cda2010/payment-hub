(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationAuditDialogController', OperationAuditDialogController);

    OperationAuditDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'OperationAudit', 'Operation'];

    function OperationAuditDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, OperationAudit, Operation) {
        var vm = this;

        vm.operationAudit = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
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
            if (vm.operationAudit.id !== null) {
                OperationAudit.update(vm.operationAudit, onSaveSuccess, onSaveError);
            } else {
                OperationAudit.save(vm.operationAudit, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:operationAuditUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.date = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
