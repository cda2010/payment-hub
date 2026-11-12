(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalApplicationDeleteController',CanalApplicationDeleteController);

        CanalApplicationDeleteController.$inject = ['$uibModalInstance', 'entity', 'CanalApplication'];

    function CanalApplicationDeleteController($uibModalInstance, entity, CanalApplication) {
        var vm = this;

        vm.canalApplication = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            CanalApplication.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
