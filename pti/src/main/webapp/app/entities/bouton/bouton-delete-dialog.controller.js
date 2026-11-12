(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BoutonDeleteController',BoutonDeleteController);

    BoutonDeleteController.$inject = ['$uibModalInstance', 'entity', 'Bouton'];

    function BoutonDeleteController($uibModalInstance, entity, Bouton) {
        var vm = this;

        vm.bouton = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Bouton.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
