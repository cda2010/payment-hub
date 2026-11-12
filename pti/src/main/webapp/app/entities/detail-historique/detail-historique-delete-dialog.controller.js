(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailHistoriqueDeleteController',DetailHistoriqueDeleteController);

    DetailHistoriqueDeleteController.$inject = ['$uibModalInstance', 'entity', 'DetailHistorique'];

    function DetailHistoriqueDeleteController($uibModalInstance, entity, DetailHistorique) {
        var vm = this;

        vm.detailHistorique = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DetailHistorique.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
