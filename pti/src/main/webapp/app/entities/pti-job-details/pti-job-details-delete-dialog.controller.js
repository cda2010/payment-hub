(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PtiJobDetailsDeleteController',PtiJobDetailsDeleteController);

    PtiJobDetailsDeleteController.$inject = ['$uibModalInstance', 'entity', 'PtiJobDetails'];

    function PtiJobDetailsDeleteController($uibModalInstance, entity, PtiJobDetails) {
        var vm = this;

        vm.ptiJobDetails = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            PtiJobDetails.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
