(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieClientDeleteController',CategorieClientDeleteController);

    CategorieClientDeleteController.$inject = ['$uibModalInstance', 'entity', 'CategorieClient'];

    function CategorieClientDeleteController($uibModalInstance, entity, CategorieClient) {
        var vm = this;

        vm.categorieClient = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CategorieClient.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
