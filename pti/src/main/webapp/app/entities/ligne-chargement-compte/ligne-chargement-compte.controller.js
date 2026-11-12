(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementCompteController', LigneChargementCompteController);

    LigneChargementCompteController.$inject = ['LigneChargementCompte'];

    function LigneChargementCompteController(LigneChargementCompte) {

        var vm = this;

        vm.ligneChargementComptes = [];

        loadAll();

        function loadAll() {
            LigneChargementCompte.query(function(result) {
                vm.ligneChargementComptes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
