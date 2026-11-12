(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeUniteOrganisationDialogController', TypeUniteOrganisationDialogController);

    TypeUniteOrganisationDialogController.$inject = ['$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeUniteOrganisation', 'TableauFrais', 'UniteOrganisation','RoleValidation'];

    function TypeUniteOrganisationDialogController ($timeout,isViewing, $scope, $stateParams, $uibModalInstance, entity, TypeUniteOrganisation, TableauFrais, UniteOrganisation,RoleValidation) {
        var vm = this;

        vm.typeUniteOrganisation = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;
        vm.roles=RoleValidation.findAuthorities();
        vm.tableaufrais = TableauFrais.query();
        //vm.uniteorganisations = UniteOrganisation.query();

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
            $scope.$emit('ptiApp:typeUniteOrganisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
            entity=null;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
