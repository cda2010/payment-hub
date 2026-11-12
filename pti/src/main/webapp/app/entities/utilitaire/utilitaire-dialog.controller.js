(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UtilitaireDialogController', UtilitaireDialogController);

    UtilitaireDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Utilitaire'];

    function UtilitaireDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Utilitaire) {
        var vm = this;

        vm.utilitaire = entity;
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
            if (vm.utilitaire.id !== null) {
                Utilitaire.update(vm.utilitaire, onSaveSuccess, onSaveError);
            } else {
                Utilitaire.save(vm.utilitaire, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:utilitaireUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
