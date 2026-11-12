(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TraitementController', TraitementController);

    TraitementController.$inject = ['Traitement'];

    function TraitementController(Traitement) {

        var vm = this;

        vm.traitements = [];

        loadAll();

        function loadAll() {
            Traitement.query(function(result) {
                vm.traitements = result;
                vm.searchQuery = null;
            });
        }
    }
})();
