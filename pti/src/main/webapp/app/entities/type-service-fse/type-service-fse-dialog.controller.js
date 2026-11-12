(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceFseDialogController', TypeServiceFseDialogController);

        TypeServiceFseDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','UniteOrganisation', 'TypeServiceFse','TypeService','CanalApplication'];

    function TypeServiceFseDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,UniteOrganisation, TypeServiceFse,TypeService, CanalApplication) {
        var vm = this;

        vm.typeServiceFse = entity;
        vm.clear = clear;     
        vm.save = save;
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
        vm.typeservices=TypeService.queryAll();
        vm.canals = [];
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        function save () {
            vm.isSaving = true;
            if (vm.typeServiceFse.id !== null) {
                TypeServiceFse.update(vm.typeServiceFse, onSaveSuccess, onSaveError);
            } else {
               
                TypeServiceFse.save(vm.typeServiceFse, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:paramCanalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
