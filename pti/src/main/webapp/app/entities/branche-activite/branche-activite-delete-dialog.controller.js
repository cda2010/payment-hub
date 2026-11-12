(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BrancheActiviteDeleteController',BrancheActiviteDeleteController);

    BrancheActiviteDeleteController.$inject = ['$uibModalInstance', 'entity', 'BrancheActivite'];

    function BrancheActiviteDeleteController($uibModalInstance, entity, BrancheActivite) {
        var vm = this;

        vm.brancheActivite = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            BrancheActivite.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
