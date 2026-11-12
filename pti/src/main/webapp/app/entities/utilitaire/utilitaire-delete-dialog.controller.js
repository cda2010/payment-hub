(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UtilitaireDeleteController',UtilitaireDeleteController);

    UtilitaireDeleteController.$inject = ['$uibModalInstance', 'entity', 'Utilitaire'];

    function UtilitaireDeleteController($uibModalInstance, entity, Utilitaire) {
        var vm = this;

        vm.utilitaire = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Utilitaire.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
