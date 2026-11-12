(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TauxChangeDeleteController',TauxChangeDeleteController);

    TauxChangeDeleteController.$inject = ['$uibModalInstance', 'entity', 'TauxChange'];

    function TauxChangeDeleteController($uibModalInstance, entity, TauxChange) {
        var vm = this;

        vm.tauxChange = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TauxChange.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
