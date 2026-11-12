(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalCommunicationDeleteController',CanalCommunicationDeleteController);

    CanalCommunicationDeleteController.$inject = ['$uibModalInstance', 'entity', 'CanalCommunication'];

    function CanalCommunicationDeleteController($uibModalInstance, entity, CanalCommunication) {
        var vm = this;

        vm.canalCommunication = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CanalCommunication.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
