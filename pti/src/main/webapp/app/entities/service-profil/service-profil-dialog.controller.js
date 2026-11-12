(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ServiceProfilDialogController', ServiceProfilDialogController);

    ServiceProfilDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ServiceProfil', 'TypeService'];

    function ServiceProfilDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ServiceProfil, TypeService) {
        var vm = this;

        vm.serviceProfil = entity;
        vm.clear = clear;
        vm.save = save;
        //vm.typeservices = TypeService.query();
        vm.typeservices=TypeService.queryAll();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.serviceProfil.id !== null) {
                ServiceProfil.update(vm.serviceProfil, onSaveSuccess, onSaveError);
            } else {
                ServiceProfil.save(vm.serviceProfil, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:serviceProfilUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
