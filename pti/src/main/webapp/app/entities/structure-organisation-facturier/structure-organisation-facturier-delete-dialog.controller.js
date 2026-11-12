(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('StructureOrganisationFacturierDeleteController',StructureOrganisationFacturierDeleteController);

    StructureOrganisationFacturierDeleteController.$inject = ['$uibModalInstance', 'entity', 'StructureOrganisationFacturier'];

    function StructureOrganisationFacturierDeleteController($uibModalInstance, entity, StructureOrganisationFacturier) {
        var vm = this;

        vm.structureOrganisationFacturier = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            StructureOrganisationFacturier.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
