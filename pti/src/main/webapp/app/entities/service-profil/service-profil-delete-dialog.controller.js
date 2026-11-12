(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ServiceProfilDeleteController',ServiceProfilDeleteController);

    ServiceProfilDeleteController.$inject = ['$uibModalInstance', 'entity', 'ServiceProfil'];

    function ServiceProfilDeleteController($uibModalInstance, entity, ServiceProfil) {
        var vm = this;

        vm.serviceProfil = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ServiceProfil.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
