(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurEmailController', RecepteurEmailController);

    RecepteurEmailController.$inject = ['RecepteurEmail'];

    function RecepteurEmailController(RecepteurEmail) {

        var vm = this;

        vm.recepteurEmails = [];

        loadAll();

        function loadAll() {
            RecepteurEmail.query(function(result) {
                vm.recepteurEmails = result;
                vm.searchQuery = null;
            });
        }
    }
})();
