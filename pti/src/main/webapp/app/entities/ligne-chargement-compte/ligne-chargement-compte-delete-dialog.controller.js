(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementCompteDeleteController',LigneChargementCompteDeleteController);

    LigneChargementCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneChargementCompte'];

    function LigneChargementCompteDeleteController($uibModalInstance, entity, LigneChargementCompte) {
        var vm = this;

        vm.ligneChargementCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneChargementCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
