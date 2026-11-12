(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreMethodeDialogController', ParametreMethodeDialogController);

    ParametreMethodeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParametreMethode', 'Methode'];

    function ParametreMethodeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParametreMethode, Methode) {
        var vm = this;

        vm.parametreMethode = entity;
        vm.clear = clear;
        vm.save = save;
        vm.methodes = Methode.query();

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
            $scope.$emit('omnicanalPresentationApp:parametreMethodeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
