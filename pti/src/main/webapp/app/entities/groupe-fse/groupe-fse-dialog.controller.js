(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeFseDialogController', GroupeFseDialogController);

        GroupeFseDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','UniteOrganisation', 'GroupeFse'];

    function GroupeFseDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,UniteOrganisation, GroupeFse) {
        var vm = this;

        vm.groupeFse = entity;
        vm.clear = clear;     
        vm.save = save;
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();  
        vm.canals = [];
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
     
        function save () {
            vm.isSaving = true;
            if (vm.groupeFse.id !== null) {
                GroupeFse.update(vm.groupeFse, onSaveSuccess, onSaveError);
            } else {
                GroupeFse.save(vm.groupeFse, onSaveSuccess, onSaveError);
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
