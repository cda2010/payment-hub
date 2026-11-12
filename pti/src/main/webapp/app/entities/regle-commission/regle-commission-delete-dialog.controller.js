(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RegleCommissionDeleteController',RegleCommissionDeleteController);

    RegleCommissionDeleteController.$inject = ['$uibModalInstance', 'entity', 'RegleCommission'];

    function RegleCommissionDeleteController($uibModalInstance, entity, RegleCommission) {
        var vm = this;

        vm.regleCommission = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RegleCommission.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
