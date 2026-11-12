(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LotDetailDeleteController',LotDetailDeleteController);

        LotDetailDeleteController.$inject = ['$uibModalInstance', 'entity', 'DetailsLot'];
        

    function LotDetailDeleteController($uibModalInstance, entity, DetailsLot) {
        var vm = this;

        vm.detailslot = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

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
