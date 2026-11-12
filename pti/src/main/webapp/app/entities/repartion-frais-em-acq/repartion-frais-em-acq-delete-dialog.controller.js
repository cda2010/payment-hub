(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartionFraisEmAcqDeleteController',RepartionFraisEmAcqDeleteController);

    RepartionFraisEmAcqDeleteController.$inject = ['$uibModalInstance', 'entity', 'RepartionFraisEmAcq'];

    function RepartionFraisEmAcqDeleteController($uibModalInstance, entity, RepartionFraisEmAcq) {
        var vm = this;

        vm.repartionFraisEmAcq = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            RepartionFraisEmAcq.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
