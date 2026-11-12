(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ApiDeleteController',ApiDeleteController);

    ApiDeleteController.$inject = ['$uibModalInstance', 'entity', 'Api'];

    function ApiDeleteController($uibModalInstance, entity, Api) {
        var vm = this;

        vm.api = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Api.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
