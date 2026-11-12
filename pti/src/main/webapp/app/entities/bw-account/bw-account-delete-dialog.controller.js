(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwAccountDeleteController',BwAccountDeleteController);

    BwAccountDeleteController.$inject = ['$uibModalInstance', 'entity', 'BwAccount'];

    function BwAccountDeleteController($uibModalInstance, entity, BwAccount) {
        var vm = this;

        vm.bwAccount = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            BwAccount.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
