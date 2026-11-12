(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BeneficiaireDialogController', BeneficiaireDialogController);

    BeneficiaireDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Beneficiaire', 'Personne', 'Operation'];

    function BeneficiaireDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Beneficiaire, Personne, Operation) {
        var vm = this;

        vm.beneficiaire = entity;
        vm.clear = clear;
        vm.save = save;
        vm.personnes = Personne.query();
        //vm.operations = Operation.query();
        vm.operations =null;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.beneficiaire.id !== null) {
                Beneficiaire.update(vm.beneficiaire, onSaveSuccess, onSaveError);
            } else {
                Beneficiaire.save(vm.beneficiaire, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:beneficiaireUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
