(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('WebserviceController', WebserviceController);

    WebserviceController.$inject = ['Webservice'];

    function WebserviceController(Webservice) {

        var vm = this;

        vm.webservices = [];

        loadAll();

        function loadAll() {
            Webservice.query(function(result) {
                vm.webservices = result;
                vm.searchQuery = null;
            });
        }
    }
})();
