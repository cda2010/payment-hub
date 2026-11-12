(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TransfertTestController', TransfertTestController);

    TransfertTestController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InterfaceUtilisateur', 'ServiceTraitement', 'ParametreUtiliateur'];

    function TransfertTestController ($timeout, $scope, $stateParams, $uibModalInstance, entity, InterfaceUtilisateur, ServiceTraitement, ParametreUtiliateur) {
        var vm = this;

        vm.interfaceUtilisateur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.servicetraitements = ServiceTraitement.query();
     
        
        //vm.parametreutiliateurs = ParametreUtiliateur.query();
        vm.onChangeService=onChangeService;
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

   function onChangeService(service) {
          ParametreUtiliateur.queryService({idservice:service},function(result){
                 vm.parametreutiliateurs =  result;
           
            });
        }


        function save () {
            vm.isSaving = true;
         
           /* if (vm.interfaceUtilisateur.id !== null) {
                InterfaceUtilisateur.update(vm.interfaceUtilisateur, onSaveSuccess, onSaveError);
            } else {
               
            }*/
           
             InterfaceUtilisateur.presentation(vm.parametreutiliateurs, onSaveSuccess, onSaveError);
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
