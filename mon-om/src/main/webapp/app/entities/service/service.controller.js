(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceController', ServiceController);

    ServiceController.$inject = ['Service'];

    function ServiceController(Service) {

        var vm = this;

        vm.services = [];

        loadAll();

        function loadAll() {
            Service.query(function(result) {
                vm.services = result;
                vm.searchQuery = null;
            });
        }
    }
})();
