(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneReglementDeleteController',LigneReglementDeleteController);

    LigneReglementDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneReglement'];

    function LigneReglementDeleteController($uibModalInstance, entity, LigneReglement) {
        var vm = this;

        vm.ligneReglement = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneReglement.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
