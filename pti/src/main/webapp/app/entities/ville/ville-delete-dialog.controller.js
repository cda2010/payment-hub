(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VilleDeleteController',VilleDeleteController);

    VilleDeleteController.$inject = ['$uibModalInstance', 'entity', 'Ville'];

    function VilleDeleteController($uibModalInstance, entity, Ville) {
        var vm = this;

        vm.ville = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Ville.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
