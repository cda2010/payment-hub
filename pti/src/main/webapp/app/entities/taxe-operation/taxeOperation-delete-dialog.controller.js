(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeOperationDeleteController',TaxeOperationDeleteController);

    TaxeOperationDeleteController.$inject = ['$uibModalInstance', 'entity', 'TaxeOperation'];

    function TaxeOperationDeleteController($uibModalInstance, entity, TaxeOperation) {
        var vm = this;

        vm.taxeOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TaxeOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
