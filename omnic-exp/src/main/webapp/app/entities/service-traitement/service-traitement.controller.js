(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ServiceTraitementController', ServiceTraitementController);

    ServiceTraitementController.$inject = ['ServiceTraitement'];

    function ServiceTraitementController(ServiceTraitement) {

        var vm = this;

        vm.serviceTraitements = [];

        loadAll();

        function loadAll() {
            ServiceTraitement.query(function(result) {
                vm.serviceTraitements = result;
                vm.searchQuery = null;
            });
        }
    }
})();
