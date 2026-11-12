(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('CompteController', CompteController);

    CompteController.$inject = ['Compte'];

    function CompteController(Compte) {

        var vm = this;

        vm.comptes = [];

        loadAll();

        function loadAll() {
            Compte.query(function(result) {
                vm.comptes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
