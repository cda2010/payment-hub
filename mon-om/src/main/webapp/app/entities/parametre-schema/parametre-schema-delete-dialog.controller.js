(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ParametreSchemaDeleteController',ParametreSchemaDeleteController);

    ParametreSchemaDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParametreSchema'];

    function ParametreSchemaDeleteController($uibModalInstance, entity, ParametreSchema) {
        var vm = this;

        vm.parametreSchema = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ParametreSchema.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
