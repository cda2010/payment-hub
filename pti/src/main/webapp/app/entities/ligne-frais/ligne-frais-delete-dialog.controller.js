(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneFraisDeleteController',LigneFraisDeleteController);

    LigneFraisDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneFrais'];

    function LigneFraisDeleteController($uibModalInstance, entity, LigneFrais) {
        var vm = this;

        vm.ligneFrais = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneFrais.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
