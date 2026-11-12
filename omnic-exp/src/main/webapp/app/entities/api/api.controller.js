(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ApiController', ApiController);

    ApiController.$inject = ['Api'];

    function ApiController(Api) {

        var vm = this;

        vm.apis = [];

        loadAll();

        function loadAll() {
            Api.query(function(result) {
                vm.apis = result;
                vm.searchQuery = null;
            });
        }
    }
})();
