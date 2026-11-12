(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneReglementDialogController', LigneReglementDialogController);

    LigneReglementDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneReglement', 'ReglementOrganisation', 'UniteOrganisation'];

    function LigneReglementDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LigneReglement, ReglementOrganisation, UniteOrganisation) {
        var vm = this;

        vm.ligneReglement = entity;
        vm.clear = clear;
        vm.save = save;
        vm.reglements = ReglementOrganisation.query();
        vm.uniteorganisations = UniteOrganisation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.ligneReglement.id !== null) {
                LigneReglement.update(vm.ligneReglement, onSaveSuccess, onSaveError);
            } else {
                LigneReglement.save(vm.ligneReglement, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneReglementUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
