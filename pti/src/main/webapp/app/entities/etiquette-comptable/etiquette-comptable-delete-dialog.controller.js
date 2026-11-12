(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteComptableDeleteController',EtiquetteComptableDeleteController);

    EtiquetteComptableDeleteController.$inject = ['$uibModalInstance', 'entity', 'EtiquetteComptable'];

    function EtiquetteComptableDeleteController($uibModalInstance, entity, EtiquetteComptable) {
        var vm = this;

        vm.etiquetteComptable = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EtiquetteComptable.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
