(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementDeleteController',LigneChargementDeleteController);

    LigneChargementDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneChargement'];

    function LigneChargementDeleteController($uibModalInstance, entity, LigneChargement) {
        var vm = this;

        vm.ligneChargement = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneChargement.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
