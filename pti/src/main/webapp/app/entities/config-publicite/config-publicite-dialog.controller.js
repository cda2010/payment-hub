(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ConfigPubliciteDialogController', ConfigPubliciteDialogController);

        ConfigPubliciteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','ConfigPublicite', 'Pays','TypeService'];

    function ConfigPubliciteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,ConfigPublicite,Pays,TypeService) {
        var vm = this;

        vm.configPublicite = entity;
        vm.clear = clear;
        vm.save = save;
        vm.pay=Pays.queryAll();
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 
  
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            vm.isSaving = true;
            if (vm.configPublicite.id !== null) {
                console.log("++++++++++222222+++++" + vm.configPublicite);
                ConfigPublicite.update(vm.configPublicite, onSaveSuccess, onSaveError);
            } else {
                ConfigPublicite.save(vm.configPublicite,onSaveSuccess,onSaveError);
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
