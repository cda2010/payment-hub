(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneRepartitionCommissionDeleteController',LigneRepartitionCommissionDeleteController);

    LigneRepartitionCommissionDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneRepartitionCommission'];

    function LigneRepartitionCommissionDeleteController($uibModalInstance, entity, LigneRepartitionCommission) {
        var vm = this;

        vm.ligneRepartitionCommission = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneRepartitionCommission.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
