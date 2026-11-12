(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementTransfertDialogController', LigneChargementTransfertDialogController);

    LigneChargementTransfertDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneChargementTransfert'];

    function LigneChargementTransfertDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneChargementTransfert) {
        var vm = this;

        vm.ligneChargementTransfert = entity;
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
            if (vm.ligneChargementTransfert.id !== null) {
                LigneChargementTransfert.update(vm.ligneChargementTransfert, onSaveSuccess, onSaveError);
            } else {
                LigneChargementTransfert.save(vm.ligneChargementTransfert, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneChargementTransfertUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
