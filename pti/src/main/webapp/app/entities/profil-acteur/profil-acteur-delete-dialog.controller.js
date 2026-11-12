(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurDeleteController',ProfilActeurDeleteController);

    ProfilActeurDeleteController.$inject = ['$uibModalInstance', 'entity', 'ProfilActeur'];

    function ProfilActeurDeleteController($uibModalInstance, entity, ProfilActeur) {
        var vm = this;

        vm.profilActeur = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ProfilActeur.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
