(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartionFraisEmAcqDialogController', RepartionFraisEmAcqDialogController);

    RepartionFraisEmAcqDialogController.$inject = ['Notification', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RepartionFraisEmAcq', 'TypeService'];

    function RepartionFraisEmAcqDialogController(Notification, $timeout, $scope, $stateParams, $uibModalInstance, entity, RepartionFraisEmAcq, TypeService) {
        var vm = this;

        vm.repartionFraisEmAcq = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeservices = TypeService.queryAll();

        $timeout(function() {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            if (vm.repartionFraisEmAcq.partAcq + vm.repartionFraisEmAcq.partEm <= 100) {
                vm.isSaving = true;
                if (vm.repartionFraisEmAcq.id !== null) {
                    RepartionFraisEmAcq.update(vm.repartionFraisEmAcq, onSaveSuccess, onSaveError);
                } else {
                    RepartionFraisEmAcq.save(vm.repartionFraisEmAcq, onSaveSuccess, onSaveError);
                }
            } else {
                Notification.error("Le total de la partEm et de la partAcq doit être inférieur ou egal à 100 !");
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:repartionFraisEmAcqUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }


    }
})();