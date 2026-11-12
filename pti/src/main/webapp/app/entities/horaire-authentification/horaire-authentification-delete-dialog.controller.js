(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HoraireAuthentificationDeleteController',HoraireAuthentificationDeleteController);

    HoraireAuthentificationDeleteController.$inject = ['$uibModalInstance', 'entity', 'HoraireAuthentification'];

    function HoraireAuthentificationDeleteController($uibModalInstance, entity, HoraireAuthentification) {
        var vm = this;

        vm.horaireAuthentification = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            HoraireAuthentification.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
