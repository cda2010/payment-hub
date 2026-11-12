(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RestController', RestController);

    RestController.$inject = ['Rest'];

    function RestController(Rest) {

        var vm = this;

        vm.rests = [];

        loadAll();

        function loadAll() {
            Rest.query(function(result) {
                vm.rests = result;
                vm.searchQuery = null;
            });
        }
    }
})();
