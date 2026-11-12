(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('InterfaceUtilisateurDeleteController',InterfaceUtilisateurDeleteController);

    InterfaceUtilisateurDeleteController.$inject = ['$uibModalInstance', 'entity', 'InterfaceUtilisateur'];

    function InterfaceUtilisateurDeleteController($uibModalInstance, entity, InterfaceUtilisateur) {
        var vm = this;

        vm.interfaceUtilisateur = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            InterfaceUtilisateur.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
