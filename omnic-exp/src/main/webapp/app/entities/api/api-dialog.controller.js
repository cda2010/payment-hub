(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ApiDialogController', ApiDialogController);

    ApiDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Api', 'Methode', 'FournisseurService'];

    function ApiDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Api, Methode, FournisseurService) {
        var vm = this;

        vm.api = entity;
        vm.clear = clear;
        vm.save = save;
        vm.methodes = Methode.query();
        vm.fournisseurservices = FournisseurService.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.api.id !== null) {
                Api.update(vm.api, onSaveSuccess, onSaveError);
            } else {
                Api.save(vm.api, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:apiUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
