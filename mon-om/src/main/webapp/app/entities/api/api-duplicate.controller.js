(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ApiDuplicateController', ApiDuplicateController);

    ApiDuplicateController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance','FournisseurService', 'entity', 'Api'];

    function ApiDuplicateController($timeout, $scope, $stateParams, $uibModalInstance,FournisseurService, entity, Api) {
        var vm = this;

        vm.api = angular.copy(entity);
        vm.api.id = null; // Reset ID for new entity
        vm.api.codeApi = null; // Reset code API for new entity   
        vm.clear = clear;
        vm.save = save;
        vm.isSaving = false;
        vm.fournisseurservices = FournisseurService.getAllFse();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            Api.duplicate({id: entity.id}, vm.api, onSaveSuccess, onSaveError);
        }  

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:apiUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})(); 