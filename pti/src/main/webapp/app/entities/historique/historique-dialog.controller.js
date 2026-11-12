(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistoriqueDialogController', HistoriqueDialogController);

    HistoriqueDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Historique'];

    function HistoriqueDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Historique) {
        var vm = this;

        vm.historique = entity;
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
            if (vm.historique.id !== null) {
                Historique.update(vm.historique, onSaveSuccess, onSaveError);
            } else {
                Historique.save(vm.historique, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:historiqueUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateHistorique = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
