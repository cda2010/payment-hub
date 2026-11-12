(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistExecutionJobDialogController', HistExecutionJobDialogController);

    HistExecutionJobDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'HistExecutionJob', 'PtiJobDetails'];

    function HistExecutionJobDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, HistExecutionJob, PtiJobDetails) {
        var vm = this;

        vm.histExecutionJob = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.ptijobdetails = PtiJobDetails.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.histExecutionJob.id !== null) {
                HistExecutionJob.update(vm.histExecutionJob, onSaveSuccess, onSaveError);
            } else {
                HistExecutionJob.save(vm.histExecutionJob, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:histExecutionJobUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.debutExecution = false;
        vm.datePickerOpenStatus.finExecution = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
