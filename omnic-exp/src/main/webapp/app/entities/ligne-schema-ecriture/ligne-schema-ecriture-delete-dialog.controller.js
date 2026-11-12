(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneSchemaEcritureDeleteController',LigneSchemaEcritureDeleteController);

    LigneSchemaEcritureDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneSchemaEcriture'];

    function LigneSchemaEcritureDeleteController($uibModalInstance, entity, LigneSchemaEcriture) {
        var vm = this;

        vm.ligneSchemaEcriture = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneSchemaEcriture.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
