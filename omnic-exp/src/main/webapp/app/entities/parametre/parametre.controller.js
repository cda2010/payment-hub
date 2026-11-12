(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreController', ParametreController);

    ParametreController.$inject = ['Parametre'];

    function ParametreController(Parametre) {

        var vm = this;

        vm.parametres = [];

        loadAll();

        function loadAll() {
            Parametre.query(function(result) {
                vm.parametres = result;
                vm.searchQuery = null;
            });
        }
    }
})();
