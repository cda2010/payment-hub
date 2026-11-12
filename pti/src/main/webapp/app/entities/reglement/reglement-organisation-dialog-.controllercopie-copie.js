(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReglementOrganisationDialogControllerCopie', ReglementOrganisationDialogControllerCopie);

    ReglementOrganisationDialogControllerCopie.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ReglementOrganisation'];

    function ReglementOrganisationDialogControllerCopie ($timeout, $scope, $stateParams, $uibModalInstance, entity, ReglementOrganisation) {
        var vm = this;

        vm.reglement = entity;
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
            if (vm.reglement.id !== null) {
                ReglementOrganisation.update(vm.reglement, onSaveSuccess, onSaveError);
            } else {
                ReglementOrganisation.save(vm.reglement, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:reglementUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateReglement = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
