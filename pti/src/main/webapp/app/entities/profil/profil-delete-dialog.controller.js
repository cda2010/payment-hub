(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilDeleteController',ProfilDeleteController);

    ProfilDeleteController.$inject = ['$uibModalInstance', 'entity', 'Profil'];

    function ProfilDeleteController($uibModalInstance, entity, Profil) {
        var vm = this;

        vm.profil = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Profil.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
