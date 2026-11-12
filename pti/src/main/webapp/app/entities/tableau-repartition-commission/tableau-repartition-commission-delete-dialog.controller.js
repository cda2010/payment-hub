(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauRepartitionCommissionDeleteController',TableauRepartitionCommissionDeleteController);

    TableauRepartitionCommissionDeleteController.$inject = ['$uibModalInstance', 'entity', 'TableauRepartitionCommission'];

    function TableauRepartitionCommissionDeleteController($uibModalInstance, entity, TableauRepartitionCommission) {
        var vm = this;

        vm.tableauRepartitionCommission = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TableauRepartitionCommission.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
