(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwCustomerValideController',BwCustomerValideController);

    BwCustomerValideController.$inject = ['$uibModalInstance', 'entity', 'BwCustomer'];

    function BwCustomerValideController($uibModalInstance, entity, BwCustomer) {
        var vm = this;

        vm.bwCustomer = entity;
        vm.clear = clear;
        vm.confirmValide = confirmValide;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmValide() {
            vm.bwCustomer.valide=true;
            BwCustomer.update(vm.bwCustomer,
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
