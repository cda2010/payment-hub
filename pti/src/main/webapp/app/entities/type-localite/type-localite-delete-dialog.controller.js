(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeLocaliteDeleteController',TypeLocaliteDeleteController);

    TypeLocaliteDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeLocalite'];

    function TypeLocaliteDeleteController($uibModalInstance, entity, TypeLocalite) {
        var vm = this;

        vm.typeLocalite = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeLocalite.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
