(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TerminalDialogController', TerminalDialogController);

    TerminalDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Terminal'];

    function TerminalDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Terminal) {
        var vm = this;

        vm.terminal = entity;
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
            if (vm.terminal.id !== null) {
                Terminal.update(vm.terminal, onSaveSuccess, onSaveError);
            } else {
                Terminal.save(vm.terminal, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:terminalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
