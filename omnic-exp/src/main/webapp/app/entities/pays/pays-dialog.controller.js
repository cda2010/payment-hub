(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('PaysDialogController', PaysDialogController);

    PaysDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Pays'];

    function PaysDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Pays) {
        var vm = this;
        vm.ajouter=ajouter;
        vm.pays = entity;
        vm.clear = clear;
        vm.save = save;
        vm.listeCOmpte=[];
vm.appel=appel;
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });


function appel(){
            
              Pays.appelApi();
           
           
          
            
        }
        
        
        function ajouter(){
            
         vm.listeCOmpte.push({compte:vm.credit,codeBanque: vm.codebanque,codeAgence: vm.codeagence,montant:vm.montant,  motif:vm.motif})   ;         
           
           
          
            
        }
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.pays.id !== null) {
                Pays.update(vm.pays, onSaveSuccess, onSaveError);
            } else {
                Pays.save(vm.pays, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:paysUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
