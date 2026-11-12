(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurTableauFraisDeleteController',ProfilActeurTableauFraisDeleteController);

    ProfilActeurTableauFraisDeleteController.$inject = ['$uibModalInstance', 'entity', 'ProfilActeurTableauFrais'];

    function ProfilActeurTableauFraisDeleteController($uibModalInstance, entity, ProfilActeurTableauFrais) {
        var vm = this;

        vm.profilActeurTableauFrais = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ProfilActeurTableauFrais.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
