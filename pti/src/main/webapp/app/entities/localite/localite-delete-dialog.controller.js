(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LocaliteDeleteController',LocaliteDeleteController);

    LocaliteDeleteController.$inject = ['$uibModalInstance', 'entity', 'Localite'];

    function LocaliteDeleteController($uibModalInstance, entity, Localite) {
        var vm = this;

        vm.localite = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Localite.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
