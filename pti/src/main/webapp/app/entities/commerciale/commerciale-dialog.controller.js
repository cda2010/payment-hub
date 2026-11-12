(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommercialeDialogController', CommercialeDialogController);

    CommercialeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Commerciale'];

    function CommercialeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Commerciale) {
        var vm = this;

        vm.commerciale = entity;
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
            if (vm.commerciale.id !== null) {
                Commerciale.update(vm.commerciale, onSaveSuccess, onSaveError);
            } else {
                Commerciale.save(vm.commerciale, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:commercialeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
