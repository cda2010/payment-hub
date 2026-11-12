(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NotificationDeleteController',NotificationDeleteController);

    NotificationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Notifications'];

    function NotificationDeleteController($uibModalInstance, entity, Notifications) {
        var vm = this;

        vm.notification = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Notifications.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
