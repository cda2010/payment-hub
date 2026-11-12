(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TraitementDialogController', TraitementDialogController);

    TraitementDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Traitement'];

    function TraitementDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Traitement) {
        var vm = this;

        vm.traitement = entity;
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
            if (vm.traitement.id !== null) {
                Traitement.update(vm.traitement, onSaveSuccess, onSaveError);
            } else {
                Traitement.save(vm.traitement, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:traitementUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
