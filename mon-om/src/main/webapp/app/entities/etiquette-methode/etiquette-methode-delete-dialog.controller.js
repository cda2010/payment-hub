(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('EtiquetteMethodeDeleteController',EtiquetteMethodeDeleteController  );

    EtiquetteMethodeDeleteController.$inject = ['$uibModalInstance', 'entity', 'EtiquetteMethode'];

    function EtiquetteMethodeDeleteController  ($uibModalInstance, entity, EtiquetteMethode) {
        var vm = this;

        vm.etiquetteMethode = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EtiquetteMethode.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
