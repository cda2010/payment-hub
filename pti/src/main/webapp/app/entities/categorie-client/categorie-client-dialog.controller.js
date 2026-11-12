(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieClientDialogController', CategorieClientDialogController);

    CategorieClientDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CategorieClient'];

    function CategorieClientDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CategorieClient) {
        var vm = this;

        vm.categorieClient = entity;
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
            if (vm.categorieClient.id !== null) {
                CategorieClient.update(vm.categorieClient, onSaveSuccess, onSaveError);
            } else {
                CategorieClient.save(vm.categorieClient, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:categorieClientUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
