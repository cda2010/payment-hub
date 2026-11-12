(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EmetteurDialogController', EmetteurDialogController);

    EmetteurDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Emetteur', 'Personne', 'Operation'];

    function EmetteurDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Emetteur, Personne, Operation) {
        var vm = this;

        vm.emetteur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.personnes = Personne.query();
        vm.operations = Operation.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.emetteur.id !== null) {
                Emetteur.update(vm.emetteur, onSaveSuccess, onSaveError);
            } else {
                Emetteur.save(vm.emetteur, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:emetteurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
