(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreDialogController', ParametreDialogController);

    ParametreDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Parametre'];

    function ParametreDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Parametre) {
        var vm = this;

        vm.parametre = entity;
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
            if (vm.parametre.id !== null) {
                Parametre.update(vm.parametre, onSaveSuccess, onSaveError);
            } else {
                Parametre.save(vm.parametre, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:parametreUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
