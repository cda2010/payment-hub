(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReglementOrganisationDeleteController',ReglementOrganisationDeleteController);

    ReglementOrganisationDeleteController.$inject = ['$uibModalInstance', 'entity', 'ReglementOrganisation'];

    function ReglementOrganisationDeleteController($uibModalInstance, entity, ReglementOrganisation) {
        var vm = this;

        vm.reglement = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ReglementOrganisation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
