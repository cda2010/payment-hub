(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('EtiquetteDialogController', EtiquetteDialogController);

    EtiquetteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Etiquette'];

    function EtiquetteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Etiquette) {
        var vm = this;

        vm.etiquette = entity;
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
            if (vm.etiquette.id !== null) {
                Etiquette.update(vm.etiquette, onSaveSuccess, onSaveError);
            } else {
                Etiquette.save(vm.etiquette, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:etiquetteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
