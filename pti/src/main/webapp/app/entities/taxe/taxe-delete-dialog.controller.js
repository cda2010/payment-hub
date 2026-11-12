(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeDeleteController',TaxeDeleteController);

    TaxeDeleteController.$inject = ['$uibModalInstance', 'entity', 'Taxe'];

    function TaxeDeleteController($uibModalInstance, entity, Taxe) {
        var vm = this;

        vm.taxe = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Taxe.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
