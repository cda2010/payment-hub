(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteDialogController', CompteDialogController);

    CompteDialogController.$inject = ['RequestUtils','$timeout','ValeurCompte','UniteOrganisationByType','TypeUOWithoutUR','Devise','EtiquetteCompte','isViewing','isEdit', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Compte', 'CompteBanque', 'CompteAutre', 'Client', 'TypeCompte', 'UniteOrganisation'];

    function CompteDialogController (RequestUtils,$timeout,ValeurCompte,UniteOrganisationByType,TypeUOWithoutUR ,Devise,EtiquetteCompte, isViewing,isEdit, $scope, $stateParams, $uibModalInstance, $q, entity, Compte, CompteBanque, CompteAutre, Client, TypeCompte, UniteOrganisation) {
        var vm = this;

        vm.compte = entity;
        vm.isViewing = isViewing;
        vm.isEdit = isEdit;
        vm.clear = clear;
        vm.save = save;
        vm.typeClient=null;
        //vm.clients = Client.queryAll();
      /*  Client.findOpListeClient({typePersonne:'MORALE'},function (result) {
            vm.clientsEntrp=result;
        });*/
        vm.clientsEntrp=null;
        vm.clientsParti=null;
       /* Client.findOpListeClient({typePersonne: 'PHYSIQUE'},function (result) {
            vm.clientsParti=result;
        });*/

        //console.log( 'client',vm.clients);
        console.log( 'client E',vm.clientsEntrp);
        console.log( 'client P',vm.clientsParti);
        console.log( 'compte-----',vm.compte);
        vm.typecomptes = TypeCompte.query();
        vm.etiquetteComptes = RequestUtils.EtiquetteCompteList.query();
        vm.devises = Devise.query();
        vm.typeuniteorganisations = TypeUOWithoutUR.query();
        vm.typeUOChange = typeUOChange;
        vm.categorieCompteChange = categorieCompteChange;
        function typeUOChange(){
        UniteOrganisationByType.queryOptimise({ typeId: vm.typeUniteOrganisationId }, function(data) {
                       vm.uniteorganisations = data;
                    }, function() {});
            }

        function categorieCompteChange(){
            if(vm.compte.firstCategorieCompte=="COMPTE_INTERNE") {
                ValeurCompte.get({}, function (data) {
                    vm.compte.valeurCompte = data.valeurCompte;
                    console.log(data)
                }, function () {
                });
            }else{
                vm.compte.valeurCompte = null;
            }
        }
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

         if (vm.isEdit || vm.isViewing) {
            if(vm.compte.estUO){
                 UniteOrganisation.get({ id: vm.compte.uniteOrganisationId }, function(data) {
                       vm.typeUniteOrganisationId = data.typeUOId;
                       vm.typeUOChange();
                    }, function() {});
            }
            if(vm.compte.categorieCompte=="BANQUE"){
                vm.compte.estCompteBanque=true;
                CompteBanque.get({ id: vm.compte.compteBanqueId }, function(data) {
                        vm.compteBanque = data;
                    }, function() {});
            }
            if(vm.compte.categorieCompte=="AUTRE"){
                vm.compte.estCompteBanque = false;
            }
         }

        function save () {
            var codeFse=null;
            if(vm.compte.estCompteBanque){
                vm.compte.categorieCompte = "BANQUE";
                vm.compte.compteBanqueDTO = vm.compteBanque;
            }else{
                if (vm.compteBanque!=null){
                    codeFse=vm.compteBanque.codeFse
                }
                vm.compte.categorieCompte = "AUTRE";
                vm.compteAutre = {codeFse:codeFse,solde:0};
              //  vm.compteAutre.solde = 0;
                vm.compte.compteAutreDTO = vm.compteAutre;
            }
            vm.isSaving = true;
            if (vm.compte.id !== null) {
                console.log('update',vm.compte);
                Compte.update(vm.compte, onSaveSuccess, onSaveError);
            } else {
                Compte.save(vm.compte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:compteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        RequestUtils.GetFournisseur.query({},function(datas){
            vm.fournisseurservices = datas;
        });

    }
})();
