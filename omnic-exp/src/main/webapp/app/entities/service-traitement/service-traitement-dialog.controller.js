(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ServiceTraitementDialogController', ServiceTraitementDialogController);

    ServiceTraitementDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ServiceTraitement', 'FournisseurService'];

    function ServiceTraitementDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ServiceTraitement, FournisseurService) {
        var vm = this;

        vm.serviceTraitement = entity;
        vm.clear = clear;
        vm.save = save;
        vm.fournisseurservices = FournisseurService.query();
       

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.serviceTraitement.id !== null) {
                ServiceTraitement.update(vm.serviceTraitement, onSaveSuccess, onSaveError);
            } else {
                ServiceTraitement.save(vm.serviceTraitement, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:serviceTraitementUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
