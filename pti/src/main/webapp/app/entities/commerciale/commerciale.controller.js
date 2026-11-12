(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommercialeController', CommercialeController);

    CommercialeController.$inject = ['Commerciale'];

    function CommercialeController(Commerciale) {

        var vm = this;

        vm.commerciales = [];

        loadAll();

        function loadAll() {
            Commerciale.query(function(result) {
                vm.commerciales = result;
                vm.searchQuery = null;
            });
        }
    }
})();
