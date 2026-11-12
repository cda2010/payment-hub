(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BoutonController', BoutonController);

    BoutonController.$inject = ['Bouton'];

    function BoutonController(Bouton) {

        var vm = this;

        vm.boutons = [];

        loadAll();

        function loadAll() {
            Bouton.query(function(result) {
                vm.boutons = result;
                vm.searchQuery = null;
            });
        }
    }
})();
