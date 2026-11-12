(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementCompteDialogController', LigneChargementCompteDialogController);

    LigneChargementCompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneChargementCompte'];

    function LigneChargementCompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneChargementCompte) {
        var vm = this;

        vm.ligneChargementCompte = entity;
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
            if (vm.ligneChargementCompte.id !== null) {
                LigneChargementCompte.update(vm.ligneChargementCompte, onSaveSuccess, onSaveError);
            } else {
                LigneChargementCompte.save(vm.ligneChargementCompte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneChargementCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
