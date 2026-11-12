(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('AgenceDialogController', AgenceDialogController);

    AgenceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Agence'];

    function AgenceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Agence) {
        var vm = this;

        vm.agence = entity;
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
            if (vm.agence.id !== null) {
                Agence.update(vm.agence, onSaveSuccess, onSaveError);
            } else {
                Agence.save(vm.agence, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:agenceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
