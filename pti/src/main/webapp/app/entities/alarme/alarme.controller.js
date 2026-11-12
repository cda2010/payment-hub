(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AlarmeController', AlarmeController);

    AlarmeController.$inject = ['Alarme'];

    function AlarmeController(Alarme) {

        var vm = this;

        vm.alarmes = [];

        loadAll();

        function loadAll() {
            Alarme.query(function(result) {
                vm.alarmes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
