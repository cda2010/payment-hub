(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TransactionComptePtiDeleteController',TransactionComptePtiDeleteController);

    TransactionComptePtiDeleteController.$inject = ['$uibModalInstance', 'entity', 'TransactionComptePti'];

    function TransactionComptePtiDeleteController($uibModalInstance, entity, TransactionComptePti) {
        var vm = this;

        vm.transactionComptePti = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TransactionComptePti.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
