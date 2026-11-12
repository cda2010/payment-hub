(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RoleValidationDialogController', RoleValidationDialogController);

    RoleValidationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RoleValidation','TypeService'];

    function RoleValidationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, RoleValidation,TypeService) {
        var vm = this;

        vm.roleValidation = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeServices=TypeService.queryAll();
        vm.roles=RoleValidation.findAuthorities();
        vm.operateurs=['EGALE', 'INFERIEUR_EGALE', 'SUPERIEUR_EGALE', 'INFERIEUR', 'SUPERIEUR'];

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.roleValidation.id !== null) {
                RoleValidation.update(vm.roleValidation, onSaveSuccess, onSaveError);
            } else {
                RoleValidation.save(vm.roleValidation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:roleValidationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
