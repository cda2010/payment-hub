(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UoDeviseDeleteController',UoDeviseDeleteController);

    UoDeviseDeleteController.$inject = ['$uibModalInstance', 'entity', 'UoDevise'];

    function UoDeviseDeleteController($uibModalInstance, entity, UoDevise) {
        var vm = this;

        vm.uoDevise = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            UoDevise.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
