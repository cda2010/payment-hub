(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeController', ListeController);

    ListeController.$inject = ['Liste'];

    function ListeController(Liste) {

        var vm = this;

        vm.listes = [];

        loadAll();

        function loadAll() {
            Liste.query(function(result) {
                vm.listes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
