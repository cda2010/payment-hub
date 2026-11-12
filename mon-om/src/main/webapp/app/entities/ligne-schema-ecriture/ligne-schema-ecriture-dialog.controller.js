(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('LigneSchemaEcritureDialogController', LigneSchemaEcritureDialogController);

    LigneSchemaEcritureDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneSchemaEcriture', 'SchemaEcriture'];

    function LigneSchemaEcritureDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneSchemaEcriture, SchemaEcriture) {
        var vm = this;

        vm.ligneSchemaEcriture = entity;
        vm.clear = clear;
        vm.save = save;
        vm.schemaecritures = SchemaEcriture.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.ligneSchemaEcriture.id !== null) {
                LigneSchemaEcriture.update(vm.ligneSchemaEcriture, onSaveSuccess, onSaveError);
            } else {
                LigneSchemaEcriture.save(vm.ligneSchemaEcriture, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:ligneSchemaEcritureUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
