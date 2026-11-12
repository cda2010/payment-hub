(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementOperationDeleteDialogController', EvenementOperationDeleteDialogController);

    EvenementOperationDeleteDialogController.$inject = ['$uibModalInstance', 'entity', 'EvenementOperation'];

    function EvenementOperationDeleteDialogController($uibModalInstance, entity, EvenementOperation) {
        var vm = this;

        vm.evenementOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EvenementOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
