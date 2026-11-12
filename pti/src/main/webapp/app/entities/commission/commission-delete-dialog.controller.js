(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommissionDeleteController',CommissionDeleteController);

    CommissionDeleteController.$inject = ['$uibModalInstance', 'entity', 'Commission'];

    function CommissionDeleteController($uibModalInstance, entity, Commission) {
        var vm = this;

        vm.commission = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Commission.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
