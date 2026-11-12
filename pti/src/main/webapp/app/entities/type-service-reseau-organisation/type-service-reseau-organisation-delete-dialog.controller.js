(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceReseauOrganisationDeleteController',TypeServiceReseauOrganisationDeleteController);

        TypeServiceReseauOrganisationDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParamCanal'];

    function TypeServiceReseauOrganisationDeleteController($uibModalInstance, entity, ParamCanal) {
        var vm = this;

        vm.typeServiceReseauOrganisation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            ParamCanal.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
