(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('FournisseurServiceDeleteController',FournisseurServiceDeleteController);

    FournisseurServiceDeleteController.$inject = ['$uibModalInstance', 'entity', 'FournisseurService'];

    function FournisseurServiceDeleteController($uibModalInstance, entity, FournisseurService) {
        var vm = this;

        vm.fournisseurService = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            FournisseurService.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
