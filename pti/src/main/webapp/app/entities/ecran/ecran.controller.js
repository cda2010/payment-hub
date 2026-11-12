(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EcranController', EcranController);

    EcranController.$inject = ['Ecran'];

    function EcranController(Ecran) {

        var vm = this;

        vm.ecrans = [];

        loadAll();

        function loadAll() {
            Ecran.query(function(result) {
                vm.ecrans = result;
                vm.searchQuery = null;
            });
        }
    }
})();
