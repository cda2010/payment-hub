(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BilanOperationDeleteController',BilanOperationDeleteController);

    BilanOperationDeleteController.$inject = ['$uibModalInstance', 'entity', 'BilanOperation'];

    function BilanOperationDeleteController($uibModalInstance, entity, BilanOperation) {
        var vm = this;

        vm.bilanOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            BilanOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
