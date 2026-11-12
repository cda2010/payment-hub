(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ApiDialogController', ApiDialogController);

    ApiDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Api', 'Methode', 'FournisseurService','Rest','Webservice'];

    function ApiDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Api, Methode, FournisseurService,Rest,Webservice) {
        var vm = this;

        vm.api = entity;
        vm.clear = clear;
        vm.save = save;
        vm.methodes = Methode.query();
        vm.fournisseurservices = FournisseurService.getAllFse();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            console.log(vm.api.type);
            if (vm.api.id !== null) {
                                Api.update(vm.api, onSaveSuccess, onSaveError);
                            } else {
                                Api.save(vm.api, onSaveSuccess, onSaveError);
                            }
                         /*
            if (vm.api.type==='REST'){
                          if (vm.api.id !== null) {
                               Rest.update(vm.api, onSaveSuccess, onSaveError);
                           } else {
                               Rest.save(vm.api, onSaveSuccess, onSaveError);
                           }
            }else if (vm.api.type==='WEBSERVICE'){
            
                            if (vm.api.id !== null) {
                                Webservice.update(vm.api, onSaveSuccess, onSaveError);
                            } else {
                                Webservice.save(vm.api, onSaveSuccess, onSaveError);
                            }
            }else{
                           
               
            }*/
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:apiUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
