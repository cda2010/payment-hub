(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementMessageDialogController', ChargementMessageDialogController);

    ChargementMessageDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ChargementMessage'];

    function ChargementMessageDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ChargementMessage) {
        var vm = this;

        vm.chargementMessage = entity;
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
            if (vm.chargementMessage.id !== null) {
                ChargementMessage.update(vm.chargementMessage, onSaveSuccess, onSaveError);
            } else {
                ChargementMessage.save(vm.chargementMessage, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:chargementMessageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.date_chargement = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
