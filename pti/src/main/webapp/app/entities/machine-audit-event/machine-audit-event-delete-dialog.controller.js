(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineAuditEventDeleteController',MachineAuditEventDeleteController);

    MachineAuditEventDeleteController.$inject = ['$uibModalInstance', 'entity', 'MachineAuditEvent'];

    function MachineAuditEventDeleteController($uibModalInstance, entity, MachineAuditEvent) {
        var vm = this;

        vm.machineAuditEvent = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            MachineAuditEvent.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
