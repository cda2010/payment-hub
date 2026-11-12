(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('SchemaEcritureController', SchemaEcritureController);

    SchemaEcritureController.$inject = ['SchemaEcriture'];

    function SchemaEcritureController(SchemaEcriture) {

        var vm = this;

        vm.schemaEcritures = [];

        loadAll();

        function loadAll() {
            SchemaEcriture.query(function(result) {
                vm.schemaEcritures = result;
                vm.searchQuery = null;
            });
        }
    }
})();
