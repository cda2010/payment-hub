(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LogOperationDialogController', LogOperationDialogController);

    LogOperationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LogOperation'];

    function LogOperationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LogOperation) {
        var vm = this;

        vm.logOperation = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.logOperation.id !== null) {
                LogOperation.update(vm.logOperation, onSaveSuccess, onSaveError);
            } else {
                LogOperation.save(vm.logOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:logOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
