(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationDeleteController',UniteOrganisationDeleteController);

    UniteOrganisationDeleteController.$inject = ['$uibModalInstance', 'entity', 'UniteOrganisation'];

    function UniteOrganisationDeleteController($uibModalInstance, entity, UniteOrganisation) {
        var vm = this;

        vm.uniteOrganisation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            UniteOrganisation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
