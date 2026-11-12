(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParametreAppDeleteController',ParametreAppDeleteController);

    ParametreAppDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParametreApp'];

    function ParametreAppDeleteController($uibModalInstance, entity, ParametreApp) {
        var vm = this;

        vm.parametreApp = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ParametreApp.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
