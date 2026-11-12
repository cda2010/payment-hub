(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteBanqueDialogController', CompteBanqueDialogController);

    CompteBanqueDialogController.$inject = ['$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CompteBanque', 'Devise'];

    function CompteBanqueDialogController ($timeout,isViewing, $scope, $stateParams, $uibModalInstance, entity, CompteBanque, Devise) {
        var vm = this;

        vm.compteBanque = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;
        vm.devises = Devise.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.compteBanque.id !== null) {
                CompteBanque.update(vm.compteBanque, onSaveSuccess, onSaveError);
            } else {
                CompteBanque.save(vm.compteBanque, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:compteBanqueUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
