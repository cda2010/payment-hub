(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailsLotDeleteController',DetailsLotDeleteController);

        DetailsLotDeleteController.$inject = ['$uibModalInstance', 'entity', 'DetailsLot'];

    function DetailsLotDeleteController($uibModalInstance, entity, DetailsLot) {
        var vm = this;

        vm.DetailsLot = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        fonction 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DetailsLot.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
