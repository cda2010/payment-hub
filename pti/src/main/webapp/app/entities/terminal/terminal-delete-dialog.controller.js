(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TerminalDeleteController',TerminalDeleteController);

    TerminalDeleteController.$inject = ['$uibModalInstance', 'entity', 'Terminal'];

    function TerminalDeleteController($uibModalInstance, entity, Terminal) {
        var vm = this;

        vm.terminal = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Terminal.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
