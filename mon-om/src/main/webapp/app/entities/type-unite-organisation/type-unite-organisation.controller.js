(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeUniteOrganisationController', TypeUniteOrganisationController);

    TypeUniteOrganisationController.$inject = ['TypeUniteOrganisation'];

    function TypeUniteOrganisationController(TypeUniteOrganisation) {

        var vm = this;

        vm.typeUniteOrganisations = [];

        loadAll();

        function loadAll() {
            TypeUniteOrganisation.query(function(result) {
                vm.typeUniteOrganisations = result;
                vm.searchQuery = null;
            });
        }
    }
})();
