(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BilanOperationDialogController', BilanOperationDialogController);

    BilanOperationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'BilanOperation', 'TypeService', 'Canal', 'Client', 'UniteOrganisation'];

    function BilanOperationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, BilanOperation, TypeService, Canal, Client, UniteOrganisation) {
        var vm = this;

        vm.bilanOperation = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.typeservices = TypeService.queryAll();
        vm.canals = Canal.query();
        vm.clients = Client.query();
        vm.uniteorganisations = UniteOrganisation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.bilanOperation.id !== null) {
                BilanOperation.update(vm.bilanOperation, onSaveSuccess, onSaveError);
            } else {
                BilanOperation.save(vm.bilanOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:bilanOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDerniereOperation = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
