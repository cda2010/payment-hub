(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurEmailDialogController', RecepteurEmailDialogController);

    RecepteurEmailDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RecepteurEmail'];

    function RecepteurEmailDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, RecepteurEmail) {
        var vm = this;

        vm.recepteurEmail = entity;
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
            if (vm.recepteurEmail.id !== null) {
                RecepteurEmail.update(vm.recepteurEmail, onSaveSuccess, onSaveError);
            } else {
                RecepteurEmail.save(vm.recepteurEmail, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:recepteurEmailUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
