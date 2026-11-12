(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineAuditEventDialogController', MachineAuditEventDialogController);

    MachineAuditEventDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'MachineAuditEvent'];

    function MachineAuditEventDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, MachineAuditEvent) {
        var vm = this;

        vm.machineAuditEvent = entity;
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
            if (vm.machineAuditEvent.id !== null) {
                MachineAuditEvent.update(vm.machineAuditEvent, onSaveSuccess, onSaveError);
            } else {
                MachineAuditEvent.save(vm.machineAuditEvent, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:machineAuditEventUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
