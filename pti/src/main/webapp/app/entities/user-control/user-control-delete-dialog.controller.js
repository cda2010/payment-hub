(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserControlDeleteController',UserControlDeleteController);

    UserControlDeleteController.$inject = ['$uibModalInstance', 'entity', 'UserControl'];

    function UserControlDeleteController($uibModalInstance, entity, UserControl) {
        var vm = this;

        vm.userControl = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            UserControl.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
