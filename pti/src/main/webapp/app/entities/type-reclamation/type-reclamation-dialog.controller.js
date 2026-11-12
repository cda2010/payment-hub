(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeReclamationDialogController', TypeReclamationDialogController);
    TypeReclamationDialogController.$inject = ['$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance', 'entity','TypeReclamation'];

    function TypeReclamationDialogController ($timeout,isViewing, $scope, $stateParams, $uibModalInstance, entity,TypeReclamation) {
        var vm = this;

        vm.typeReclamation = entity;
        vm.isViewing = isViewing;
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
            if (vm.typeReclamation.id !== null) {
                TypeReclamation.update(vm.typeReclamation, onSaveSuccess, onSaveError);
            } else {
                vm.typeReclamation.dateHeureCreation=new Date();
                TypeReclamation.save(vm.typeReclamation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typeReclamationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
