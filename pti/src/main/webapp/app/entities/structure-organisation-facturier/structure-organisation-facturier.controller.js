(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('StructureOrganisationFacturierController', StructureOrganisationFacturierController);

    StructureOrganisationFacturierController.$inject = ['StructureOrganisationFacturier'];

    function StructureOrganisationFacturierController(StructureOrganisationFacturier) {

        var vm = this;

        vm.structureOrganisationFacturiers = [];

        loadAll();

        function loadAll() {
            StructureOrganisationFacturier.query(function(result) {
                vm.structureOrganisationFacturiers = result;
                vm.searchQuery = null;
            });
        }
    }
})();
