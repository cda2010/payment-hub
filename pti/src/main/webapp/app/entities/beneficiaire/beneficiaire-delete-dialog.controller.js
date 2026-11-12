(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BeneficiaireDeleteController',BeneficiaireDeleteController);

    BeneficiaireDeleteController.$inject = ['$uibModalInstance', 'entity', 'Beneficiaire'];

    function BeneficiaireDeleteController($uibModalInstance, entity, Beneficiaire) {
        var vm = this;

        vm.beneficiaire = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Beneficiaire.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
