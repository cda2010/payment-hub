(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneFraisDialogController', LigneFraisDialogController);

    LigneFraisDialogController.$inject = ['Notification', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneFrais', 'TableauFrais'];

    function LigneFraisDialogController(Notification, $timeout, $scope, $stateParams, $uibModalInstance, entity, LigneFrais, TableauFrais) {
        var vm = this;

        vm.ligneFrais = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function() {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            if (vm.ligneFrais.minimun < vm.ligneFrais.maximun) {
                vm.isSaving = true;
                if (vm.ligneFrais.id !== null) {
                    LigneFrais.update(vm.ligneFrais, onSaveSuccess, onSaveError);
                } else {
                    LigneFrais.save(vm.ligneFrais, onSaveSuccess, onSaveError);
                }
            } else {
                Notification.error("Le montant max doit être supérieur au montant min! ");
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:ligneFraisUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }


    }
})();