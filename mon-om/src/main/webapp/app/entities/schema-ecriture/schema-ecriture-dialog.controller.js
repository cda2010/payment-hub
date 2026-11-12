(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('SchemaEcritureDialogController', SchemaEcritureDialogController);

    SchemaEcritureDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'SchemaEcriture','TypeService'];

    function SchemaEcritureDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, SchemaEcriture,TypeService) {
        var vm = this;

        vm.schemaEcriture = entity;
        vm.clear = clear;
        vm.save = save;
vm.typeServices=TypeService.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.schemaEcriture.id !== null) {
                SchemaEcriture.update(vm.schemaEcriture, onSaveSuccess, onSaveError);
            } else {
                SchemaEcriture.save(vm.schemaEcriture, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:schemaEcritureUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
