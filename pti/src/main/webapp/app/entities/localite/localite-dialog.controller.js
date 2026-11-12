(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LocaliteDialogController', LocaliteDialogController);

    LocaliteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Localite'];

    function LocaliteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Localite) {
        var vm = this;

        vm.localite = entity;
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
            if (vm.localite.id !== null) {
                Localite.update(vm.localite, onSaveSuccess, onSaveError);
            } else {
                Localite.save(vm.localite, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:localiteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
