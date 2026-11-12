(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalCommunicationController', CanalCommunicationController);

    CanalCommunicationController.$inject = ['CanalCommunication'];

    function CanalCommunicationController(CanalCommunication) {

        var vm = this;

        vm.canalCommunications = [];

        loadAll();

        function loadAll() {
            CanalCommunication.query(function(result) {
                vm.canalCommunications = result;
                vm.searchQuery = null;
            });
        }
    }
})();
