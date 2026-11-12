(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteAutreDialogController', CompteAutreDialogController);

    CompteAutreDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CompteAutre'];

    function CompteAutreDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CompteAutre) {
        var vm = this;

        vm.compteAutre = entity;
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
            if (vm.compteAutre.id !== null) {
                CompteAutre.update(vm.compteAutre, onSaveSuccess, onSaveError);
            } else {
                CompteAutre.save(vm.compteAutre, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:compteAutreUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
