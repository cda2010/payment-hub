(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurTableauFraisController', ProfilActeurTableauFraisController);

    ProfilActeurTableauFraisController.$inject = ['ProfilActeurTableauFrais'];

    function ProfilActeurTableauFraisController(ProfilActeurTableauFrais) {

        var vm = this;

        vm.profilActeurTableauFrais = [];

        loadAll();

        function loadAll() {
            ProfilActeurTableauFrais.query(function(result) {
                vm.profilActeurTableauFrais = result;
                vm.searchQuery = null;
            });
        }
    }
})();
