(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreMethodeController', ParametreMethodeController);

    ParametreMethodeController.$inject = ['ParametreMethode','$stateParams'];

    function ParametreMethodeController(ParametreMethode,$stateParams) {

        var vm = this;

        vm.parametreMethodes = [];

        loadAll();

        function loadAll() {
            ParametreMethode.queryMethode({idMethode:$stateParams.idmethode},function(result) {
                vm.parametreMethodes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
