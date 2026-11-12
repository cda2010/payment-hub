(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreUtiliateurController', ParametreUtiliateurController);

    ParametreUtiliateurController.$inject = ['ParametreUtiliateur','$stateParams'];

    function ParametreUtiliateurController(ParametreUtiliateur,$stateParams) {

        var vm = this;

        vm.parametreUtiliateurs = [];

        loadAll();

        function loadAll() {
            ParametreUtiliateur.queryInterface({idInterface:$stateParams.idinterface},function(result) {
                vm.parametreUtiliateurs = result;
                vm.searchQuery = null;
            });
        }
    }
})();
