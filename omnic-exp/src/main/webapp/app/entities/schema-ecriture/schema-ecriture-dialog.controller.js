(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('SchemaEcritureDialogController', SchemaEcritureDialogController);

    SchemaEcritureDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'SchemaEcriture'];

    function SchemaEcritureDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, SchemaEcriture) {
        var vm = this;

        vm.schemaEcriture = entity;
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
            if (vm.schemaEcriture.id !== null) {
                SchemaEcriture.update(vm.schemaEcriture, onSaveSuccess, onSaveError);
            } else {
                SchemaEcriture.save(vm.schemaEcriture, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:schemaEcritureUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
