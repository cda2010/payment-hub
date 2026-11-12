(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('InterfaceUtilisateurController', InterfaceUtilisateurController);

    InterfaceUtilisateurController.$inject = ['InterfaceUtilisateur'];

    function InterfaceUtilisateurController(InterfaceUtilisateur) {

        var vm = this;

        vm.interfaceUtilisateurs = [];

        loadAll();

        function loadAll() {
            InterfaceUtilisateur.query(function(result) {
                vm.interfaceUtilisateurs = result;
                vm.searchQuery = null;
            });
        }
    }
})();
