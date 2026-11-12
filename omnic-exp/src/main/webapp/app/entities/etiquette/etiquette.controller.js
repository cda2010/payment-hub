(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('EtiquetteController', EtiquetteController);

    EtiquetteController.$inject = ['Etiquette'];

    function EtiquetteController(Etiquette) {

        var vm = this;

        vm.etiquettes = [];

        loadAll();

        function loadAll() {
            Etiquette.query(function(result) {
                vm.etiquettes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
