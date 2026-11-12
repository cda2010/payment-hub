(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionFraisDeleteController',RepartitionFraisDeleteController);

    RepartitionFraisDeleteController.$inject = ['$uibModalInstance', 'entity', 'RepartitionFrais'];

    function RepartitionFraisDeleteController($uibModalInstance, entity, RepartitionFrais) {
        var vm = this;

        vm.repartitionFrais = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RepartitionFrais.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
