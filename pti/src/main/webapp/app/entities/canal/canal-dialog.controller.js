(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalDialogController', CanalDialogController);

    CanalDialogController.$inject = ['isViewing', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Canal', 'Operation', 'TableauFrais', 'TypeService'];

    function CanalDialogController(isViewing, $timeout, $scope, $stateParams, $uibModalInstance, entity, Canal, Operation, TableauFrais, TypeService) {
        var vm = this;

        vm.canal = entity;
        vm.clear = clear;
        vm.save = save;
       // vm.operations = Operation.query();
        vm.operations = null;
        //vm.tableaufrais = TableauFrais.query();
        vm.tableaufrais = null;
        vm.typeservices = TypeService.queryAll();
        vm.isViewing = isViewing;

        $timeout(function() {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            vm.isSaving = true;
            if (vm.canal.id !== null) {
                Canal.update(vm.canal, onSaveSuccess, onSaveError);
            } else {
                Canal.save(vm.canal, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:canalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }


    }
})();