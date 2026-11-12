(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneMoraleDeleteController',PersonneMoraleDeleteController);

    PersonneMoraleDeleteController.$inject = ['$uibModalInstance', 'entity', 'PersonneMorale'];

    function PersonneMoraleDeleteController($uibModalInstance, entity, PersonneMorale) {
        var vm = this;

        vm.personneMorale = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            PersonneMorale.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
