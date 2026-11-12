(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LienDialogController', LienDialogController);

    LienDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Lien', 'Personne'];

    function LienDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Lien, Personne) {
        var vm = this;

        vm.lien = entity;
        vm.clear = clear;
        vm.save = save;
        vm.personnes = Personne.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.lien.id !== null) {
                Lien.update(vm.lien, onSaveSuccess, onSaveError);
            } else {
                Lien.save(vm.lien, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:lienUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
