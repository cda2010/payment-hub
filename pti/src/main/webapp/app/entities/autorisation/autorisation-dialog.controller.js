(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AutorisationDialogController', AutorisationDialogController);

    AutorisationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Autorisation', 'UniteOrganisation', 'TypeService'];

    function AutorisationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Autorisation, UniteOrganisation, TypeService) {
        var vm = this;

        vm.autorisation = entity;
        vm.clear = clear;
        vm.save = save;
        vm.uniteorganisations = UniteOrganisation.query();
        vm.typeservices = TypeService.queryAll();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.autorisation.id !== null) {
                Autorisation.update(vm.autorisation, onSaveSuccess, onSaveError);
            } else {
                Autorisation.save(vm.autorisation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:autorisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
