(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RoleValidationDeleteController',RoleValidationDeleteController);

    RoleValidationDeleteController.$inject = ['$uibModalInstance', 'entity', 'RoleValidation'];

    function RoleValidationDeleteController($uibModalInstance, entity, RoleValidation) {
        var vm = this;

        vm.roleValidation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RoleValidation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
