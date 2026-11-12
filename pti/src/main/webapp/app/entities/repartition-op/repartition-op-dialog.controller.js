(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionOpDialogController', RepartitionOpDialogController);

    RepartitionOpDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RepartitionOp', 'Operation'];

    function RepartitionOpDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, RepartitionOp, Operation) {
        var vm = this;

        vm.repartitionOp = entity;
        vm.clear = clear;
        vm.save = save;
        vm.operations = Operation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.repartitionOp.id !== null) {
                RepartitionOp.update(vm.repartitionOp, onSaveSuccess, onSaveError);
            } else {
                RepartitionOp.save(vm.repartitionOp, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:repartitionOpUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
