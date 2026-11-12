(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeCompteDialogController', TypeCompteDialogController);

    TypeCompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeCompte', 'Compte'];

    function TypeCompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeCompte, Compte) {
        var vm = this;

        vm.typeCompte = entity;
        vm.clear = clear;
        vm.save = save;
        vm.comptes = Compte.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.typeCompte.id !== null) {
                TypeCompte.update(vm.typeCompte, onSaveSuccess, onSaveError);
            } else {
                TypeCompte.save(vm.typeCompte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typeCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
