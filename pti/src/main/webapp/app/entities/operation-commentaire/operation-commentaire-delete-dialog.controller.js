(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationCommentaireDeleteController',OperationCommentaireDeleteController);

    OperationCommentaireDeleteController.$inject = ['$uibModalInstance', 'entity', 'OperationCommentaire'];

    function OperationCommentaireDeleteController($uibModalInstance, entity, OperationCommentaire) {
        var vm = this;

        vm.operationCommentaire = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            OperationCommentaire.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
