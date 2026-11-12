(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BrancheActiviteDialogController', BrancheActiviteDialogController);

    BrancheActiviteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'BrancheActivite'];

    function BrancheActiviteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, BrancheActivite) {
        var vm = this;

        vm.brancheActivite = entity;
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
            if (vm.brancheActivite.id !== null) {
                BrancheActivite.update(vm.brancheActivite, onSaveSuccess, onSaveError);
            } else {
                BrancheActivite.save(vm.brancheActivite, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:brancheActiviteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
