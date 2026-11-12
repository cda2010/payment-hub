(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationSuivisDeleteController',OperationSuivisDeleteController);

    OperationSuivisDeleteController.$inject = ['$uibModalInstance', 'entity', 'OperationSuivis'];

    function OperationSuivisDeleteController($uibModalInstance, entity, OperationSuivis) {
        var vm = this;

        vm.operationSuivis = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            OperationSuivis.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
