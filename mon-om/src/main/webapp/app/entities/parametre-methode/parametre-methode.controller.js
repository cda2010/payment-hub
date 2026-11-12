(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ParametreMethodeController', ParametreMethodeController);

    ParametreMethodeController.$inject = ['ParametreMethode','$stateParams'];
    // ParametreMethodeController.$inject = ['ParametreMethode','$stateParams','Etiquette'];

    function ParametreMethodeController(ParametreMethode,$stateParams) {
    // function ParametreMethodeController(ParametreMethode,$stateParams,Etiquette) {

        var vm = this;

        vm.parametreMethodes = [];
        vm.methodeId = null;
        // vm.etiquettes=Etiquette.query();

        loadAll();

        function loadAll() {
            ParametreMethode.queryMethode({idMethode:$stateParams.idmethode},function(result) {
                vm.parametreMethodes = result;
                if (vm.parametreMethodes.length > 0)
                    vm.methodeId = vm.parametreMethodes[0].methode.api.id;
                console.log(vm.methodeId)
                vm.searchQuery = null;
            });
        }
    }
})();
