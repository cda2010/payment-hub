(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceMethodeController', ServiceMethodeController);

    ServiceMethodeController.$inject = ['ServiceMethode','$stateParams','ServiceTraitement'];

    function ServiceMethodeController(ServiceMethode,$stateParams,ServiceTraitement) {

        var vm = this;

        vm.serviceMethodes = [];

        loadAll();
        vm.service= ServiceTraitement.get({id:$stateParams.idService});

        function loadAll() {
            ServiceMethode.queryService({idService:$stateParams.idService},function(result) {
                vm.serviceMethodes = result;
                vm.searchQuery = null;
            });
        }
    }
})();
