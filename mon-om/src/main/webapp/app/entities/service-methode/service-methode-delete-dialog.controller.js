(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceMethodeDeleteController',ServiceMethodeDeleteController);

    ServiceMethodeDeleteController.$inject = ['$uibModalInstance', 'entity', 'ServiceMethode'];

    function ServiceMethodeDeleteController($uibModalInstance, entity, ServiceMethode) {
        var vm = this;

        vm.serviceMethode = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ServiceMethode.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
