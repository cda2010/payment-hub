(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtapeServiceDialogController', EtapeServiceDialogController);

    EtapeServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EtapeService', 'TypeService'];

    function EtapeServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EtapeService, TypeService) {
        var vm = this;

        vm.etapeService = entity;
        vm.clear = clear;
        vm.save = save;   
        vm.typeservices=TypeService.queryAll();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.etapeService.id !== null) {
                EtapeService.update(vm.etapeService, onSaveSuccess, onSaveError);
            } else {
                console.log("+++++++++++++++"+vm.etapeService.actionService)
                EtapeService.save(vm.etapeService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:etapeServiceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
