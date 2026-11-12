(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ExportTableDialogController', ExportTableDialogController);

    ExportTableDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ExportTable'];

    function ExportTableDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ExportTable) {
        var vm = this;

        vm.exportTable = entity;
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
            if (vm.exportTable.id !== null) {
                ExportTable.update(vm.exportTable, onSaveSuccess, onSaveError);
            } else {
                ExportTable.save(vm.exportTable, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:exportTableUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateGeneration = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
