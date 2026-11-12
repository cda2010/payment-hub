(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .controller('LigneOperationController', LigneOperationController);

    LigneOperationController.$inject = ['Operation', 'TypeService', 'FournisseurService','$uibModalInstance', 'entity'];

    function LigneOperationController(Operation, TypeService, FournisseurService, $uibModalInstance, entity) {

        var vm = this;
        vm.clear = clear;
        vm.typeServices = TypeService.query();
        vm.fournisseurServices = FournisseurService.query();
        vm.operations = [];
        vm.afficheLigneTransaction = afficheLigneTransaction;
        loadAll();

        function loadAll() {
            afficheLigneTransaction(entity);
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function afficheLigneTransaction(operation) {
            console.log(operation);
            vm.defOperation=operation;
            Operation.queryLigne({id: operation.id}, function (result) {
                vm.trans = result;
                console.log(result);
            });
        }
    }
})();
