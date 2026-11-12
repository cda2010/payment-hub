(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DeviseDeleteController',DeviseDeleteController);

    DeviseDeleteController.$inject = ['$uibModalInstance', 'entity', 'Devise'];

    function DeviseDeleteController($uibModalInstance, entity, Devise) {
        var vm = this;

        vm.devise = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Devise.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
