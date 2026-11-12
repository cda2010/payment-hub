(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteCompteDeleteController',EtiquetteCompteDeleteController);

    EtiquetteCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'EtiquetteCompte'];

    function EtiquetteCompteDeleteController($uibModalInstance, entity, EtiquetteCompte) {
        var vm = this;

        vm.etiquetteCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EtiquetteCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
