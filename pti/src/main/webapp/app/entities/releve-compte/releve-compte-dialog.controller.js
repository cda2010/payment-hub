(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReleveCompteDialogController', ReleveCompteDialogController);

    ReleveCompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ReleveCompte', 'DetailReleveCompte'];

    function ReleveCompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ReleveCompte, DetailReleveCompte) {
        var vm = this;

        vm.releveCompte = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.detailrelevecomptes = DetailReleveCompte.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.releveCompte.id !== null) {
                ReleveCompte.update(vm.releveCompte, onSaveSuccess, onSaveError);
            } else {
                ReleveCompte.save(vm.releveCompte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:releveCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
