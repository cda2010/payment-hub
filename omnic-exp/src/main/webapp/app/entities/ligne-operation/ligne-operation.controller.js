(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneOperationController', LigneOperationController);

    LigneOperationController.$inject = ['LigneOperation'];

    function LigneOperationController(LigneOperation) {

        var vm = this;

        vm.ligneOperations = [];

        loadAll();

        function loadAll() {
            LigneOperation.query(function(result) {
                vm.ligneOperations = result;
                vm.searchQuery = null;
            });
        }
    }
})();
