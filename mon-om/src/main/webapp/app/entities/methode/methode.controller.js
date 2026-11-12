(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('MethodeController', MethodeController);

    MethodeController.$inject = ['Methode','$stateParams'];

    function MethodeController(Methode,$stateParams) {

        var vm = this;

        vm.methodes = [];

        loadAll();

        function loadAll() {
            Methode.queryApi({idApi:$stateParams.idapi },function(result) {
                vm.methodes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
