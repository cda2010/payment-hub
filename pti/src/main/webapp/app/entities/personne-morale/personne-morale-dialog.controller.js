(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneMoraleDialogController', PersonneMoraleDialogController);

    PersonneMoraleDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'PersonneMorale'];

    function PersonneMoraleDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, PersonneMorale) {
        var vm = this;

        vm.personneMorale = entity;
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
            if (vm.personneMorale.id !== null) {
                PersonneMorale.update(vm.personneMorale, onSaveSuccess, onSaveError);
            } else {
                PersonneMorale.save(vm.personneMorale, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:personneMoraleUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
