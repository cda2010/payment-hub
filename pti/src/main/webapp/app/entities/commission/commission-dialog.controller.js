(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommissionDialogController', CommissionDialogController);

    CommissionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Commission', 'Operation', 'UniteOrganisation'];

    function CommissionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Commission, Operation, UniteOrganisation) {
        var vm = this;

        vm.commission = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        //vm.operations = Operation.query();
        vm.operations = null;
        vm.uniteorganisations = UniteOrganisation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.commission.id !== null) {
                Commission.update(vm.commission, onSaveSuccess, onSaveError);
            } else {
                Commission.save(vm.commission, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:commissionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.datePaiement = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
