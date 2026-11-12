(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TauxChangeDialogController', TauxChangeDialogController);

    TauxChangeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TauxChange', 'Devise'];

    function TauxChangeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TauxChange, Devise) {
        var vm = this;

        vm.tauxChange = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.devises = Devise.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.tauxChange.id !== null) {
                TauxChange.update(vm.tauxChange, onSaveSuccess, onSaveError);
            } else {
                TauxChange.save(vm.tauxChange, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:tauxChangeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateTaux = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
