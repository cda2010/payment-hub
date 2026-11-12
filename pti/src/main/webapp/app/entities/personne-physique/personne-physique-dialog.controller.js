(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonnePhysiqueDialogController', PersonnePhysiqueDialogController);

    PersonnePhysiqueDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'PersonnePhysique', 'Nationalite'];

    function PersonnePhysiqueDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, PersonnePhysique, Nationalite) {
        var vm = this;

        vm.personnePhysique = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.nationalites = Nationalite.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.personnePhysique.id !== null) {
                PersonnePhysique.update(vm.personnePhysique, onSaveSuccess, onSaveError);
            } else {
                PersonnePhysique.save(vm.personnePhysique, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:personnePhysiqueUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateNaissance = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
