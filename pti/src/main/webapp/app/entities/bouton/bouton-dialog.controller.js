(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BoutonDialogController', BoutonDialogController);

    BoutonDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Bouton'];

    function BoutonDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Bouton) {
        var vm = this;

        vm.bouton = entity;
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
            if (vm.bouton.id !== null) {
                Bouton.update(vm.bouton, onSaveSuccess, onSaveError);
            } else {
                Bouton.save(vm.bouton, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:boutonUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
