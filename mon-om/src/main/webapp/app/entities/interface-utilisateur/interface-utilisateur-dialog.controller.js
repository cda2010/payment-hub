(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('InterfaceUtilisateurDialogController', InterfaceUtilisateurDialogController);

    InterfaceUtilisateurDialogController.$inject = ['Methode','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InterfaceUtilisateur', 'ServiceTraitement', 'ParametreUtiliateur','FournisseurService'];

    function InterfaceUtilisateurDialogController (Methode,$timeout, $scope, $stateParams, $uibModalInstance, entity, InterfaceUtilisateur, ServiceTraitement, ParametreUtiliateur,FournisseurService) {
        var vm = this;

        vm.interfaceUtilisateur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.getMethode=getMethode;
        vm.servicetraitements = ServiceTraitement.query();
        vm.parametreutiliateurs = ParametreUtiliateur.query();
       vm.fournisseurservices= FournisseurService.getAllFse();
        if (vm.interfaceUtilisateur.id !== null && vm.interfaceUtilisateur.fournisseurService!==null ) {
                 vm.methodes=Methode.queryFse({idfse:vm.interfaceUtilisateur.fournisseurService.id});
           }
        function getMethode(fournisseurService){
            vm.methodes=Methode.queryFse({idfse:fournisseurService.id});
        }
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
            $scope.$emit('omnicanalApp:interfaceUtilisateurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
