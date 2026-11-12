(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementTransfertDeleteController',ChargementTransfertDeleteController);

    ChargementTransfertDeleteController.$inject = ['$uibModalInstance', 'entity', 'ChargementTransfert'];

    function ChargementTransfertDeleteController($uibModalInstance, entity, ChargementTransfert) {
        var vm = this;

        vm.chargementTransfert = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ChargementTransfert.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
