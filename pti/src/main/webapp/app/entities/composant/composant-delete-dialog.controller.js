(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComposantDeleteController',ComposantDeleteController);

    ComposantDeleteController.$inject = ['$uibModalInstance', 'entity', 'Composant'];

    function ComposantDeleteController($uibModalInstance, entity, Composant) {
        var vm = this;

        vm.composant = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Composant.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
