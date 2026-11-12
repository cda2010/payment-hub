(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementCompteDeleteController',ChargementCompteDeleteController);

    ChargementCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'ChargementCompte'];

    function ChargementCompteDeleteController($uibModalInstance, entity, ChargementCompte) {
        var vm = this;

        vm.chargementCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ChargementCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
