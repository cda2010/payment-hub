(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieServiceDeleteController',CategorieServiceDeleteController);

        CategorieServiceDeleteController.$inject = ['$uibModalInstance', 'entity', 'CategorieService'];

    function CategorieServiceDeleteController($uibModalInstance, entity, CategorieService) {
        var vm = this;

        vm.categorieService = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            CategorieService.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
