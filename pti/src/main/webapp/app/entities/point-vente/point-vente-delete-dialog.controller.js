(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointVenteDeleteController',PointVenteDeleteController);

    PointVenteDeleteController.$inject = ['$uibModalInstance', 'entity', 'PointVente'];

    function PointVenteDeleteController($uibModalInstance, entity, PointVente) {
        var vm = this;

        vm.pointVente = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            PointVente.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
