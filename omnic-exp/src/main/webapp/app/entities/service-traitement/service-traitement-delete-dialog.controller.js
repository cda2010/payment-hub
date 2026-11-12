(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ServiceTraitementDeleteController',ServiceTraitementDeleteController);

    ServiceTraitementDeleteController.$inject = ['$uibModalInstance', 'entity', 'ServiceTraitement'];

    function ServiceTraitementDeleteController($uibModalInstance, entity, ServiceTraitement) {
        var vm = this;

        vm.serviceTraitement = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ServiceTraitement.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
