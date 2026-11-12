(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDialogController', ListeDialogController);

    ListeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Liste'];

    function ListeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Liste) {
        var vm = this;

        vm.liste = entity;
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
            if (vm.liste.id !== null) {
                Liste.update(vm.liste, onSaveSuccess, onSaveError);
            } else {
                Liste.save(vm.liste, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:listeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
