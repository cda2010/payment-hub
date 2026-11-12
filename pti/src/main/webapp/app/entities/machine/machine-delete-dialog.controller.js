(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineDeleteController', MachineDeleteController);

    MachineDeleteController.$inject = ['$uibModalInstance', 'entity', 'Machine'];

    function MachineDeleteController($uibModalInstance, entity, Machine) {
        var vm = this;

        vm.machine = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        vm.confirmDeleteMachine = confirmDeleteMachine;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete(id) {
            Machine.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }

        function confirmDeleteMachine(id) {
            Machine.deleteMachine({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
