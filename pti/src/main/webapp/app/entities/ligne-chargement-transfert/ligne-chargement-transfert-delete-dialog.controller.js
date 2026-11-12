(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementTransfertDeleteController',LigneChargementTransfertDeleteController);

    LigneChargementTransfertDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneChargementTransfert'];

    function LigneChargementTransfertDeleteController($uibModalInstance, entity, LigneChargementTransfert) {
        var vm = this;

        vm.ligneChargementTransfert = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneChargementTransfert.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
