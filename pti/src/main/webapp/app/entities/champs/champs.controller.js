(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChampsController', ChampsController);

    ChampsController.$inject = ['Champs'];

    function ChampsController(Champs) {

        var vm = this;

        vm.champs = [];

        loadAll();

        function loadAll() {
            Champs.query(function(result) {
                vm.champs = result;
                vm.searchQuery = null;
            });
        }
    }
})();
