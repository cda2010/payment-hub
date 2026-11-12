(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('FournisseurServiceController', FournisseurServiceController);

    FournisseurServiceController.$inject = ['FournisseurService'];

    function FournisseurServiceController(FournisseurService) {

        var vm = this;

        vm.fournisseurServices = [];

        loadAll();

        function loadAll() {
            FournisseurService.query(function(result) {
                vm.fournisseurServices = result;
                vm.searchQuery = null;
            });
        }
    }
})();
