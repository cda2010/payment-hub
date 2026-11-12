(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('InterfaceUtilisateurDialogController', InterfaceUtilisateurDialogController);

    InterfaceUtilisateurDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InterfaceUtilisateur', 'ServiceTraitement', 'ParametreUtiliateur'];

    function InterfaceUtilisateurDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, InterfaceUtilisateur, ServiceTraitement, ParametreUtiliateur) {
        var vm = this;

        vm.interfaceUtilisateur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.servicetraitements = ServiceTraitement.query();
        vm.parametreutiliateurs = ParametreUtiliateur.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.interfaceUtilisateur.id !== null) {
                InterfaceUtilisateur.update(vm.interfaceUtilisateur, onSaveSuccess, onSaveError);
            } else {
                InterfaceUtilisateur.save(vm.interfaceUtilisateur, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:interfaceUtilisateurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
