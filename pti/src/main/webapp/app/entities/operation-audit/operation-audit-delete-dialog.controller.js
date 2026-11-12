(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationAuditDeleteController',OperationAuditDeleteController);

    OperationAuditDeleteController.$inject = ['$uibModalInstance', 'entity', 'OperationAudit'];

    function OperationAuditDeleteController($uibModalInstance, entity, OperationAudit) {
        var vm = this;

        vm.operationAudit = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            OperationAudit.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
