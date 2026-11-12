(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientCreaDeleteController',ClientCreaDeleteController);

    ClientCreaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Client'];

    function ClientCreaDeleteController($uibModalInstance, entity, Client) {
        var vm = this;

        vm.client = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Client.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
