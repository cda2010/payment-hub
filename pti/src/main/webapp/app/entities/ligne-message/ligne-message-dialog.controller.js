(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneMessageDialogController', LigneMessageDialogController);

    LigneMessageDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneMessage', 'ChargementMessage'];

    function LigneMessageDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneMessage, ChargementMessage) {
        var vm = this;

        vm.ligneMessage = entity;
        vm.clear = clear;
        vm.save = save;
        vm.chargementmessages = ChargementMessage.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.ligneMessage.id !== null) {
                LigneMessage.update(vm.ligneMessage, onSaveSuccess, onSaveError);
            } else {
                LigneMessage.save(vm.ligneMessage, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneMessageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
