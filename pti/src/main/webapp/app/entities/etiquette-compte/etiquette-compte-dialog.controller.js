(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteCompteDialogController', EtiquetteCompteDialogController);

    EtiquetteCompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EtiquetteCompte','TypeUOWithoutUR'];

    function EtiquetteCompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EtiquetteCompte,TypeUOWithoutUR) {
        var vm = this;

        vm.etiquetteCompte = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeUos = TypeUOWithoutUR.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.etiquetteCompte.id !== null) {
                EtiquetteCompte.update(vm.etiquetteCompte, onSaveSuccess, onSaveError);
            } else {
                EtiquetteCompte.save(vm.etiquetteCompte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:etiquetteCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
