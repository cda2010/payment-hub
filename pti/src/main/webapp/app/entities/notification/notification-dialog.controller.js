(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NotificationDialogController', NotificationDialogController);

    NotificationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Notifications'];

    function NotificationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Notifications) {
        var vm = this;

        vm.notification = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.notification.id !== null) {
                Notifications.update(vm.notification, onSaveSuccess, onSaveError);
            } else {
                Notifications.save(vm.notification, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:notificationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateHeureEnvoie = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
