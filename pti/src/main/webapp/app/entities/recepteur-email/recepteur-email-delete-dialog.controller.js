(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurEmailDeleteController',RecepteurEmailDeleteController);

    RecepteurEmailDeleteController.$inject = ['$uibModalInstance', 'entity', 'RecepteurEmail'];

    function RecepteurEmailDeleteController($uibModalInstance, entity, RecepteurEmail) {
        var vm = this;

        vm.recepteurEmail = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RecepteurEmail.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
