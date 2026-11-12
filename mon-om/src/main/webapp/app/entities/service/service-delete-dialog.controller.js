(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceDeleteController',ServiceDeleteController);

    ServiceDeleteController.$inject = ['$uibModalInstance', 'entity', 'Service'];

    function ServiceDeleteController($uibModalInstance, entity, Service) {
        var vm = this;

        vm.service = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Service.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
