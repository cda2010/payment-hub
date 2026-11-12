(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupRepartitionDialogController', GroupRepartitionDialogController);

    GroupRepartitionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'GroupRepartition','TypeService'];

    function GroupRepartitionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, GroupRepartition, TypeService) {
        var vm = this;

        vm.groupRepartition = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeServices=TypeService.queryAll();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.groupRepartition.id !== null) {
                GroupRepartition.update(vm.groupRepartition, onSaveSuccess, onSaveError);
            } else {
                GroupRepartition.save(vm.groupRepartition, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:groupRepartitionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
