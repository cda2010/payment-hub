(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('CompteDialogController', CompteDialogController);

    CompteDialogController.$inject = ['Agence','TypeService','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Compte','Relation','TypeCompte','FournisseurService'];

    function CompteDialogController (Agence,TypeService,$timeout, $scope, $stateParams, $uibModalInstance, entity, Compte,Relation,TypeCompte,FournisseurService) {
        var vm = this;
vm.typeServices =TypeService.query();
        vm.compte = entity;
        vm.clear = clear;
        vm.save = save;
       
        vm.getAgence=getAgence;
        vm.getCodeAgence=getCodeAgence;
       
         
  vm.relations = Relation.queryRel();
  if (vm.compte.id !== null){
      vm.cpte= vm.compte.id ;
      getAgence (vm.compte.fseTeneur);
  }else{
      if (angular.isDefined($stateParams.type) ){
             vm.compte= {typeCompte:{id:$stateParams.type}, fseTitulaire:{id:$stateParams.titulaire}, fseTeneur:{id:$stateParams.teneur}};
              getAgence (vm.compte.fseTeneur);
         }
        vm.cpte= 0 ;
  }
  function getCodeAgence(agence){
       vm.compte.codeAgence=agence.codeAgence;
  }
 function getAgence (fse){
     Agence.queryFse({fseId:fse.id}, function(data){
         vm.agences=data;
        
     });
 }
        
  vm.comptes=Compte.queryMirroir({idCompte:  vm.cpte});
    vm.fournisseurServices= FournisseurService.getAllFse();
    vm.typecomptes = TypeCompte.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.compte.id !== null) {
                Compte.update(vm.compte, onSaveSuccess, onSaveError);
            } else {
                Compte.save(vm.compte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:compteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
