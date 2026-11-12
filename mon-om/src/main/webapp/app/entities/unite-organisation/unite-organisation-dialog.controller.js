(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('UniteOrganisationDialogController', UniteOrganisationDialogController);

    UniteOrganisationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'UniteOrganisation'];

    function UniteOrganisationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, UniteOrganisation) {
        var vm = this;

        vm.uniteOrganisation = entity;
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
            if (vm.uniteOrganisation.id !== null) {
                UniteOrganisation.update(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            } else {
                UniteOrganisation.save(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:uniteOrganisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
