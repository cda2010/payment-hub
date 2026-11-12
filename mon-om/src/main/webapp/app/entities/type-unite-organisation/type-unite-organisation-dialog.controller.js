(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeUniteOrganisationDialogController', TypeUniteOrganisationDialogController);

    TypeUniteOrganisationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeUniteOrganisation'];

    function TypeUniteOrganisationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeUniteOrganisation) {
        var vm = this;

        vm.typeUniteOrganisation = entity;
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
            if (vm.typeUniteOrganisation.id !== null) {
                TypeUniteOrganisation.update(vm.typeUniteOrganisation, onSaveSuccess, onSaveError);
            } else {
                TypeUniteOrganisation.save(vm.typeUniteOrganisation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:typeUniteOrganisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
