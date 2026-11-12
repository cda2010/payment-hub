(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreUtiliateurDialogController', ParametreUtiliateurDialogController);

    ParametreUtiliateurDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParametreUtiliateur', 'InterfaceUtilisateur'];

    function ParametreUtiliateurDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParametreUtiliateur, InterfaceUtilisateur) {
        var vm = this;

        vm.parametreUtiliateur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.interfaceutilisateurs = InterfaceUtilisateur.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.parametreUtiliateur.id !== null) {
                ParametreUtiliateur.update(vm.parametreUtiliateur, onSaveSuccess, onSaveError);
            } else {
                ParametreUtiliateur.save(vm.parametreUtiliateur, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:parametreUtiliateurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
