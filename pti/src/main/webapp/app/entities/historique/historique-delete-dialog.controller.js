(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistoriqueDeleteController',HistoriqueDeleteController);

    HistoriqueDeleteController.$inject = ['$uibModalInstance', 'entity', 'Historique'];

    function HistoriqueDeleteController($uibModalInstance, entity, Historique) {
        var vm = this;

        vm.historique = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Historique.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
