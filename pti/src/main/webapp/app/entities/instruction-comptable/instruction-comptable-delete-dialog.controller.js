(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InstructionComptableDeleteController',InstructionComptableDeleteController);

    InstructionComptableDeleteController.$inject = ['$uibModalInstance', 'entity', 'InstructionComptable'];

    function InstructionComptableDeleteController($uibModalInstance, entity, InstructionComptable) {
        var vm = this;

        vm.instructionComptable = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            InstructionComptable.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
