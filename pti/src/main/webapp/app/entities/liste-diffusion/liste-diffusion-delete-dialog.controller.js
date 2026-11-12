(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDiffusionDeleteController',ListeDiffusionDeleteController);

    ListeDiffusionDeleteController.$inject = ['$uibModalInstance', 'entity', 'ListeDiffusion'];

    function ListeDiffusionDeleteController($uibModalInstance, entity, ListeDiffusion) {
        var vm = this;

        vm.listeDiffusion = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ListeDiffusion.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
