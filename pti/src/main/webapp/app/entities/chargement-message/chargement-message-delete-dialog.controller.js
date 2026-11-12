(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementMessageDeleteController',ChargementMessageDeleteController);

    ChargementMessageDeleteController.$inject = ['$uibModalInstance', 'entity', 'ChargementMessage'];

    function ChargementMessageDeleteController($uibModalInstance, entity, ChargementMessage) {
        var vm = this;

        vm.chargementMessage = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ChargementMessage.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
