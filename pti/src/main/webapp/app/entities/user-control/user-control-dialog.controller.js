(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserControlDialogController', UserControlDialogController);

    UserControlDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'UserControl'];

    function UserControlDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, UserControl) {
        var vm = this;

        vm.userControl = entity;
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
            if (vm.userControl.id !== null) {
                UserControl.update(vm.userControl, onSaveSuccess, onSaveError);
            } else {
                UserControl.save(vm.userControl, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:userControlUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
