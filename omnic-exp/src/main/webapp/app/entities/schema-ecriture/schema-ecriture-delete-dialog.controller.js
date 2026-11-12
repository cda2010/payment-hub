(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('SchemaEcritureDeleteController',SchemaEcritureDeleteController);

    SchemaEcritureDeleteController.$inject = ['$uibModalInstance', 'entity', 'SchemaEcriture'];

    function SchemaEcritureDeleteController($uibModalInstance, entity, SchemaEcriture) {
        var vm = this;

        vm.schemaEcriture = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            SchemaEcriture.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
