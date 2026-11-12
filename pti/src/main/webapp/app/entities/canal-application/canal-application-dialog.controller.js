(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalApplicationDialogController', CanalApplicationDialogController);

    CanalApplicationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CanalApplication'];

    function CanalApplicationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CanalApplication) {
        var vm = this;

        vm.canalApplication = entity;
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
            if (vm.canalApplication.id !== null) {
                CanalApplication.update(vm.canalApplication, onSaveSuccess, onSaveError);
            } else {
                console.log("+++++++++++++++"+vm.canalApplication.application)
                CanalApplication.save(vm.canalApplication, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:canalApplicationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
