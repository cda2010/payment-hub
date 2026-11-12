(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LogOperationDeleteController',LogOperationDeleteController);

    LogOperationDeleteController.$inject = ['$uibModalInstance', 'entity', 'LogOperation'];

    function LogOperationDeleteController($uibModalInstance, entity, LogOperation) {
        var vm = this;

        vm.logOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LogOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
