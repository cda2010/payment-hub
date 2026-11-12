(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitCanalDeleteController',ProduitCanalDeleteController);

    ProduitCanalDeleteController.$inject = ['$uibModalInstance', 'entity', 'ProduitCanal'];

    function ProduitCanalDeleteController($uibModalInstance, entity, ProduitCanal) {
        var vm = this;

        vm.produitCanal = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ProduitCanal.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
