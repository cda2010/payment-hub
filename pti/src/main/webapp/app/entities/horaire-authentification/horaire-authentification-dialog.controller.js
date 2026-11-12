(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HoraireAuthentificationDialogController', HoraireAuthentificationDialogController);

    HoraireAuthentificationDialogController.$inject = ['DateUtils','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'HoraireAuthentification'];

    function HoraireAuthentificationDialogController (DateUtils,$timeout, $scope, $stateParams, $uibModalInstance, entity, HoraireAuthentification) {
        var vm = this;

        vm.horaireAuthentification = entity;
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
        if (vm.horaireAuthentification.id != null) {
            console.log(vm.horaireAuthentification);
            vm.horaireAuthentification.dateDebut = DateUtils.convertLocalDateFromServer(vm.horaireAuthentification.dateDebut);
            vm.horaireAuthentification.dateFin = DateUtils.convertLocalDateFromServer(vm.horaireAuthentification.dateFin);
            
        }

        function save () {
            vm.isSaving = true;
            if (vm.horaireAuthentification.id !== null) {
                HoraireAuthentification.update(vm.horaireAuthentification, onSaveSuccess, onSaveError);
            } else {
                HoraireAuthentification.save(vm.horaireAuthentification, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:horaireAuthentificationUpdate', result);
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
