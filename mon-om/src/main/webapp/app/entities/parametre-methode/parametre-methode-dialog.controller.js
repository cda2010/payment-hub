(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ParametreMethodeDialogController', ParametreMethodeDialogController);

    ParametreMethodeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParametreMethode', 'Methode', 'EtiquetteMethode'];

    function ParametreMethodeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParametreMethode, Methode, EtiquetteMethode) {
        var vm = this;

        vm.parametreMethode = entity;
        vm.clear = clear;
        vm.save = save;
        vm.methodes = Methode.query();
        vm.etiquetteMethodes = EtiquetteMethode.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.parametreMethode.id !== null) {
                ParametreMethode.update(vm.parametreMethode, onSaveSuccess, onSaveError);
            } else {
                ParametreMethode.save(vm.parametreMethode, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:parametreMethodeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
