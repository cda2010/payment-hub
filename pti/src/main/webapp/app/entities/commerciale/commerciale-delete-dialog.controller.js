(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommercialeDeleteController',CommercialeDeleteController);

    CommercialeDeleteController.$inject = ['$uibModalInstance', 'entity', 'Commerciale'];

    function CommercialeDeleteController($uibModalInstance, entity, Commerciale) {
        var vm = this;

        vm.commerciale = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Commerciale.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
