(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AlarmeDeleteController',AlarmeDeleteController);

    AlarmeDeleteController.$inject = ['$uibModalInstance', 'entity', 'Alarme'];

    function AlarmeDeleteController($uibModalInstance, entity, Alarme) {
        var vm = this;

        vm.alarme = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Alarme.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
