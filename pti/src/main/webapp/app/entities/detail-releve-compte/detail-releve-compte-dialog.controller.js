(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailReleveCompteDialogController', DetailReleveCompteDialogController);

    DetailReleveCompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DetailReleveCompte', 'ReleveCompte'];

    function DetailReleveCompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, DetailReleveCompte, ReleveCompte) {
        var vm = this;

        vm.detailReleveCompte = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.relevecomptes = ReleveCompte.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.detailReleveCompte.id !== null) {
                DetailReleveCompte.update(vm.detailReleveCompte, onSaveSuccess, onSaveError);
            } else {
                DetailReleveCompte.save(vm.detailReleveCompte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:detailReleveCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.date = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
