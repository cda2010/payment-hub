(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionOpDeleteController',RepartitionOpDeleteController);

    RepartitionOpDeleteController.$inject = ['$uibModalInstance', 'entity', 'RepartitionOp'];

    function RepartitionOpDeleteController($uibModalInstance, entity, RepartitionOp) {
        var vm = this;

        vm.repartitionOp = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RepartitionOp.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
