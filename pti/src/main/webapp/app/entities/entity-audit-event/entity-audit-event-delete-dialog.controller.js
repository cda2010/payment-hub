(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EntityAuditEventDeleteController',EntityAuditEventDeleteController);

    EntityAuditEventDeleteController.$inject = ['$uibModalInstance', 'entity', 'EntityAuditEvent'];

    function EntityAuditEventDeleteController($uibModalInstance, entity, EntityAuditEvent) {
        var vm = this;

        vm.entityAuditEvent = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EntityAuditEvent.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
