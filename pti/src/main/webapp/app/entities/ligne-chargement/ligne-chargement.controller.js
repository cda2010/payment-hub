(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementController', LigneChargementController);

    LigneChargementController.$inject = ['LigneChargement'];

    function LigneChargementController(LigneChargement) {

        var vm = this;

        vm.ligneChargements = [];

        loadAll();

        function loadAll() {
            LigneChargement.query(function(result) {
                vm.ligneChargements = result;
                vm.searchQuery = null;
            });
        }
    }
})();
