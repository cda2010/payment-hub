(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementTransfertController', LigneChargementTransfertController);

    LigneChargementTransfertController.$inject = ['LigneChargementTransfert'];

    function LigneChargementTransfertController(LigneChargementTransfert) {

        var vm = this;

        vm.ligneChargementTransferts = [];

        loadAll();

        function loadAll() {
            LigneChargementTransfert.query(function(result) {
                vm.ligneChargementTransferts = result;
                vm.searchQuery = null;
            });
        }
    }
})();
