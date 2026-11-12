(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteAutreDeleteController',CompteAutreDeleteController);

    CompteAutreDeleteController.$inject = ['$uibModalInstance', 'entity', 'CompteAutre'];

    function CompteAutreDeleteController($uibModalInstance, entity, CompteAutre) {
        var vm = this;

        vm.compteAutre = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CompteAutre.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
