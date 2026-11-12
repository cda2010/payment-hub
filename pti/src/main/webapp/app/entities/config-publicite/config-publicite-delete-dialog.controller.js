(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ConfigPubliciteDeleteController',ConfigPubliciteDeleteController);

        ConfigPubliciteDeleteController.$inject = ['$uibModalInstance', 'entity', 'ConfigPublicite'];

    function ConfigPubliciteDeleteController($uibModalInstance, entity, ConfigPublicite) {
        var vm = this;

        vm.configPublicite = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            ConfigPublicite.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
