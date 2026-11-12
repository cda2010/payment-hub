(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurAlarmeDialogController', RecepteurAlarmeDialogController);

    RecepteurAlarmeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RecepteurAlarme','isViewing'];

    function RecepteurAlarmeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, RecepteurAlarme,isViewing) {
        var vm = this;

        vm.recepteurAlarme = entity;
        vm.clear = clear;
        vm.save = save;
        vm.isViewing=isViewing;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        function save () {
            vm.isSaving = true;
            if (vm.recepteurAlarme.id !== null) {
                RecepteurAlarme.update(vm.recepteurAlarme, onSaveSuccess, onSaveError);
            } else {
                RecepteurAlarme.save(vm.recepteurAlarme, onSaveSuccess, onSaveError);
            }
        }
        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:recepteurAlarmeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }
        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
