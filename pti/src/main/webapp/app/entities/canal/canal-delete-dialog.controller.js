(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalDeleteController',CanalDeleteController);

    CanalDeleteController.$inject = ['$uibModalInstance', 'entity', 'Canal'];

    function CanalDeleteController($uibModalInstance, entity, Canal) {
        var vm = this;

        vm.canal = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Canal.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
