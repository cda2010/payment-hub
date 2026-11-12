(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EntityAuditEventDialogController', EntityAuditEventDialogController);

    EntityAuditEventDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EntityAuditEvent'];

    function EntityAuditEventDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EntityAuditEvent) {
        var vm = this;

        vm.entityAuditEvent = entity;
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
            if (vm.entityAuditEvent.id !== null) {
                EntityAuditEvent.update(vm.entityAuditEvent, onSaveSuccess, onSaveError);
            } else {
                EntityAuditEvent.save(vm.entityAuditEvent, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:entityAuditEventUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
