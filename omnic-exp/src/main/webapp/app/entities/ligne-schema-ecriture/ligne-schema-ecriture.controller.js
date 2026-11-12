(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneSchemaEcritureController', LigneSchemaEcritureController);

    LigneSchemaEcritureController.$inject = ['LigneSchemaEcriture','$stateParams'];

    function LigneSchemaEcritureController(LigneSchemaEcriture,$stateParams) {

        var vm = this;

        vm.ligneSchemaEcritures = [];

        loadAll();

        function loadAll() {
            LigneSchemaEcriture.querySchema({idschema:$stateParams.idSchema},function(result) {
                vm.ligneSchemaEcritures = result;
                vm.searchQuery = null;
            });
        }
    }
})();
