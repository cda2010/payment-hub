(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreSchemaController', ParametreSchemaController);

    ParametreSchemaController.$inject = ['ParametreSchema','$stateParams'];

    function ParametreSchemaController(ParametreSchema,$stateParams) {

        var vm = this;

        vm.parametreSchemas = [];
vm.schema=$stateParams.idSchema;
        loadAll();

        function loadAll() {
            ParametreSchema.queryLigne({idligne:$stateParams.idligne},function(result) {
                vm.parametreSchemas = result;
                vm.searchQuery = null;
            });
        }
    }
})();
