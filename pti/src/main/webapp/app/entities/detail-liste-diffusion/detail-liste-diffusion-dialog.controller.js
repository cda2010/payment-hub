(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailListeDiffusionDialogController', DetailListeDiffusionDialogController);

    DetailListeDiffusionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DetailListeDiffusion', 'ListeDiffusion'];

    function DetailListeDiffusionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, DetailListeDiffusion, ListeDiffusion) {
        var vm = this;

        vm.detailListeDiffusion = entity;
        vm.clear = clear;
        vm.save = save;
        vm.listediffusions = ListeDiffusion.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.detailListeDiffusion.id !== null) {
                DetailListeDiffusion.update(vm.detailListeDiffusion, onSaveSuccess, onSaveError);
            } else {
                DetailListeDiffusion.save(vm.detailListeDiffusion, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:detailListeDiffusionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
