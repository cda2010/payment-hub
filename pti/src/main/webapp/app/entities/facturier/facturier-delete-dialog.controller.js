(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FacturierDeleteController',FacturierDeleteController);

    FacturierDeleteController.$inject = ['$uibModalInstance', 'entity', 'Facturier'];

    function FacturierDeleteController($uibModalInstance, entity, Facturier) {
        var vm = this;

        vm.facturier = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Facturier.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
