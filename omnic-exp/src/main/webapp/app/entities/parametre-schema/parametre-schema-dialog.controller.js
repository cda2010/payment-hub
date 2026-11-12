(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreSchemaDialogController', ParametreSchemaDialogController);

    ParametreSchemaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParametreSchema', 'LigneSchemaEcriture'];

    function ParametreSchemaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParametreSchema, LigneSchemaEcriture) {
        var vm = this;

        vm.parametreSchema = entity;
        vm.clear = clear;
        vm.save = save;
        vm.lignes = LigneSchemaEcriture.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.parametreSchema.id !== null) {
                ParametreSchema.update(vm.parametreSchema, onSaveSuccess, onSaveError);
            } else {
                ParametreSchema.save(vm.parametreSchema, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:parametreSchemaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
