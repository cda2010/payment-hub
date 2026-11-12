(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComptesDeleteController',ComptesDeleteController);

    ComptesDeleteController.$inject = ['$uibModalInstance', 'entity', 'Comptes'];

    function ComptesDeleteController($uibModalInstance, entity, Comptes) {
        var vm = this;

        vm.comptes = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        console.log(entity);
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Comptes.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
