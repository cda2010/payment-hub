(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MotifOperationDeleteController',MotifOperationDeleteController);

    MotifOperationDeleteController.$inject = ['$uibModalInstance', 'entity', 'MotifOperation'];

    function MotifOperationDeleteController($uibModalInstance, entity, MotifOperation) {
        var vm = this;

        vm.motifOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            MotifOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
