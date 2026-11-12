(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteBanqueDeleteController',CompteBanqueDeleteController);

    CompteBanqueDeleteController.$inject = ['$uibModalInstance', 'entity', 'CompteBanque'];

    function CompteBanqueDeleteController($uibModalInstance, entity, CompteBanque) {
        var vm = this;

        vm.compteBanque = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CompteBanque.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
