(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LotDeleteController',LotDeleteController);

        LotDeleteController.$inject = ['$uibModalInstance', 'entity', 'Lot'];
        

    function LotDeleteController($uibModalInstance, entity, Lot) {
        var vm = this;

        vm.lot = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Lot.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }

        
    }
})();
