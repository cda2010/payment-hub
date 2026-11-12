(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EmetteurDeleteController',EmetteurDeleteController);

    EmetteurDeleteController.$inject = ['$uibModalInstance', 'entity', 'Emetteur'];

    function EmetteurDeleteController($uibModalInstance, entity, Emetteur) {
        var vm = this;

        vm.emetteur = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Emetteur.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
