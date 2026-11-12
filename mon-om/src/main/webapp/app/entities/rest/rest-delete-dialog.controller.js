(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RestDeleteController',RestDeleteController);

    RestDeleteController.$inject = ['$uibModalInstance', 'entity', 'Rest'];

    function RestDeleteController($uibModalInstance, entity, Rest) {
        var vm = this;

        vm.rest = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Rest.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
