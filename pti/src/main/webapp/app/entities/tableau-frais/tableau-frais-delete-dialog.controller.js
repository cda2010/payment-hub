(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauFraisDeleteController',TableauFraisDeleteController);

    TableauFraisDeleteController.$inject = ['$uibModalInstance', 'entity', 'TableauFrais'];

    function TableauFraisDeleteController($uibModalInstance, entity, TableauFrais) {
        var vm = this;

        vm.tableauFrais = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TableauFrais.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
