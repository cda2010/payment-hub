(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurAlarmeDeleteController',RecepteurAlarmeDeleteController);

    RecepteurAlarmeDeleteController.$inject = ['$uibModalInstance', 'entity', 'RecepteurAlarme'];

    function RecepteurAlarmeDeleteController($uibModalInstance, entity, RecepteurAlarme) {
        var vm = this;

        vm.recepteurAlarme = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RecepteurAlarme.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
