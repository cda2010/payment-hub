(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailListeDiffusionDeleteController',DetailListeDiffusionDeleteController);

    DetailListeDiffusionDeleteController.$inject = ['$uibModalInstance', 'entity', 'DetailListeDiffusion'];

    function DetailListeDiffusionDeleteController($uibModalInstance, entity, DetailListeDiffusion) {
        var vm = this;

        vm.detailListeDiffusion = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DetailListeDiffusion.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
