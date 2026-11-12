(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneOperationDeleteController',LigneOperationDeleteController);

    LigneOperationDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneOperation'];

    function LigneOperationDeleteController($uibModalInstance, entity, LigneOperation) {
        var vm = this;

        vm.ligneOperation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneOperation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
