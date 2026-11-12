(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeServiceDialogController', TypeServiceDialogController);

    TypeServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeService'];

    function TypeServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeService) {
        var vm = this;

        vm.typeService = entity;
        vm.clear = clear;
        vm.save = save;
       

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.typeService.id !== null) {
                TypeService.update(vm.typeService, onSaveSuccess, onSaveError);
            } else {
                TypeService.save(vm.typeService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:typeServiceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
