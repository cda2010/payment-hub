(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FournisseurServiceDialogController', FournisseurServiceDialogController);

    FournisseurServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'FournisseurService', 'UniteOrganisation', 'Pays'];

    function FournisseurServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, FournisseurService, UniteOrganisation, Pays) {
        var vm = this;

        vm.fournisseurService = entity;
        vm.clear = clear;
        vm.save = save;
        vm.uniteorganisations = UniteOrganisation.query();
        vm.pays = Pays.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.fournisseurService.id !== null) {
                FournisseurService.update(vm.fournisseurService, onSaveSuccess, onSaveError);
            } else {
                FournisseurService.save(vm.fournisseurService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:fournisseurServiceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
