(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementDialogController', LigneChargementDialogController);

    LigneChargementDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneChargement'];

    function LigneChargementDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneChargement) {
        var vm = this;

        vm.ligneChargement = entity;
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
            if (vm.ligneChargement.id !== null) {
                LigneChargement.update(vm.ligneChargement, onSaveSuccess, onSaveError);
            } else {
                LigneChargement.save(vm.ligneChargement, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneChargementUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
