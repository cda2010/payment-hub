(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('MethodeDialogController', MethodeDialogController);

    MethodeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Methode', 'Api', 'ParametreMethode'];

    function MethodeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Methode, Api, ParametreMethode) {
        var vm = this;

        vm.methode = entity;
        vm.clear = clear;
        vm.save = save;
        vm.apis = Api.query();
        vm.parametremethodes = ParametreMethode.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.methode.id !== null) {
                Methode.update(vm.methode, onSaveSuccess, onSaveError);
            } else {
                Methode.save(vm.methode, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:methodeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
