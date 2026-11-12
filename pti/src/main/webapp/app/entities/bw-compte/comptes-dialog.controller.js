(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComptesDialogController', ComptesDialogController);

    ComptesDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Comptes','FournisseurService'];

    function ComptesDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Comptes,FournisseurService) {
        var vm = this;

        vm.compte = entity;
        vm.clear = clear;
        vm.save = save;
        vm.fse = FournisseurService.query();

        console.log(entity);
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {

            vm.isSaving = true;
            vm.compte.codeFse = vm.compte.fse.codeFse;
            vm.compte.codeFseProprietaire = vm.compte.fseProprietaire.codeFse;
            console.log(vm.compte);
            if (vm.compte.id !== null) {
                Comptes.update(vm.compte, onSaveSuccess, onSaveError);
            } else {
                Comptes.save(vm.compte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('compteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
