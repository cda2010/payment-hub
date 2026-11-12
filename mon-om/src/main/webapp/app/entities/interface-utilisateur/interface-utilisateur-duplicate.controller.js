(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('InterfaceUtilisateurDuplicateController', InterfaceUtilisateurDuplicateController);

    InterfaceUtilisateurDuplicateController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InterfaceUtilisateur','FournisseurService','Methode'];

    function InterfaceUtilisateurDuplicateController ($timeout, $scope, $stateParams, $uibModalInstance, entity, InterfaceUtilisateur, FournisseurService, Methode) {
        var vm = this;

        vm.interfaceUtilisateur = angular.copy(entity);
        vm.interfacecopy = angular.copy(entity);
        vm.interfaceUtilisateur.id = null;
        vm.fournisseurservices = FournisseurService.getAllFse();
        vm.clear = clear;
        vm.save = save;
        vm.getMethode = getMethode;
        if (vm.interfacecopy.id !== null && vm.interfacecopy.fournisseurService!==null ) {
            vm.methodes=Methode.queryFse({idfse:vm.interfacecopy.fournisseurService.id});
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
            InterfaceUtilisateur.duplicate({id: entity.id}, vm.interfaceUtilisateur, onSaveSuccess, onSaveError);
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