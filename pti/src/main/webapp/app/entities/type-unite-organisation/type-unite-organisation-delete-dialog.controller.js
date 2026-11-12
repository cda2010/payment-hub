(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeUniteOrganisationDeleteController',TypeUniteOrganisationDeleteController);

    TypeUniteOrganisationDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeUniteOrganisation'];

    function TypeUniteOrganisationDeleteController($uibModalInstance, entity, TypeUniteOrganisation) {
        var vm = this;

        vm.typeUniteOrganisation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeUniteOrganisation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
