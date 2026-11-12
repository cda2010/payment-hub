(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NationaliteDeleteController',NationaliteDeleteController);

    NationaliteDeleteController.$inject = ['$uibModalInstance', 'entity', 'Nationalite'];

    function NationaliteDeleteController($uibModalInstance, entity, Nationalite) {
        var vm = this;

        vm.nationalite = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Nationalite.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
