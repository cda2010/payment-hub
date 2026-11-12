(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParametreAppDialogController', ParametreAppDialogController);

    ParametreAppDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParametreApp'];

    function ParametreAppDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParametreApp) {
        var vm = this;

        vm.parametreApp = entity;
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
            if (vm.parametreApp.id !== null) {
                ParametreApp.update(vm.parametreApp, onSaveSuccess, onSaveError);
            } else {
                ParametreApp.save(vm.parametreApp, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:parametreAppUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
