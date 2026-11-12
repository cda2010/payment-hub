(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AlarmeDialogController', AlarmeDialogController);

    AlarmeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Alarme'];

    function AlarmeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Alarme) {
        var vm = this;

        vm.alarme = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.alarme.id !== null) {
                Alarme.update(vm.alarme, onSaveSuccess, onSaveError);
            } else {
                Alarme.save(vm.alarme, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:alarmeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
