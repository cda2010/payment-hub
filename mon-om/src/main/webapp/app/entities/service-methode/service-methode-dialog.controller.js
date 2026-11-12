(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceMethodeDialogController', ServiceMethodeDialogController);

    ServiceMethodeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ServiceMethode','Methode','ServiceTraitement'];

    function ServiceMethodeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ServiceMethode,Methode,ServiceTraitement) {
        var vm = this;
 
        vm.serviceMethode = entity;
        vm.clear = clear;
        vm.save = save;
         
         
         
         
          vm.service= ServiceTraitement.get({id:$stateParams.idService},function(data){
              
              vm.methodes=Methode.queryFse({idfse:data.fournisseurService.id});
          });
          
         
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.serviceMethode.id !== null) {
                ServiceMethode.update(vm.serviceMethode, onSaveSuccess, onSaveError);
            } else {
                ServiceMethode.save(vm.serviceMethode, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:serviceMethodeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
