(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('TypeCompteController', TypeCompteController);

    TypeCompteController.$inject = ['TypeCompte'];

    function TypeCompteController(TypeCompte) {

        var vm = this;

        vm.typeComptes = [];

        loadAll();

        function loadAll() {
            TypeCompte.query(function(result) {
                vm.typeComptes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
