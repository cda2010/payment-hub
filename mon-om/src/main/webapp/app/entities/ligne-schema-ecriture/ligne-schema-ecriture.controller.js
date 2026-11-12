(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('LigneSchemaEcritureController', LigneSchemaEcritureController);

    LigneSchemaEcritureController.$inject = ['LigneSchemaEcriture','$stateParams','$state'];

    function LigneSchemaEcritureController(LigneSchemaEcriture,$stateParams,$state) {

        var vm = this;
vm.retour=retour;
        vm.ligneSchemaEcritures = [];

        loadAll();
function retour(){
    $state.go('schema-ecriture',{typeService:$stateParams.typeService});
}
        function loadAll() {
            LigneSchemaEcriture.querySchema({idschema:$stateParams.idSchema},function(result) {
                vm.ligneSchemaEcritures = result;
                vm.searchQuery = null;
            });
        }
    }
})();
