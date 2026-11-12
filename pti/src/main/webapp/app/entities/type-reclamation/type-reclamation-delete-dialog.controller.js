(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeReclamationDeleteController',TypeReclamationDeleteController);

    TypeReclamationDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeReclamation'];

    function TypeReclamationDeleteController($uibModalInstance, entity, TypeReclamation) {
        var vm = this;

        vm.typeReclamation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeReclamation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
