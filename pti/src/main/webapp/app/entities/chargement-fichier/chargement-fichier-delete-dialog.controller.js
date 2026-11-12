(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementFichierDeleteController',ChargementFichierDeleteController);

    ChargementFichierDeleteController.$inject = ['$uibModalInstance', 'entity', 'ChargementFichier'];

    function ChargementFichierDeleteController($uibModalInstance, entity, ChargementFichier) {
        var vm = this;

        vm.chargementFichier = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ChargementFichier.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
