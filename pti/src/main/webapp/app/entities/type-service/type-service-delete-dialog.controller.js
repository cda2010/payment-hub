(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceDeleteController',TypeServiceDeleteController);

    TypeServiceDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeService'];

    function TypeServiceDeleteController($uibModalInstance, entity, TypeService) {
        var vm = this;

        vm.typeService = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeService.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
