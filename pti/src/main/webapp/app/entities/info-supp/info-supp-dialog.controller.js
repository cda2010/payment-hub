(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InfoSuppDialogController', InfoSuppDialogController);

    InfoSuppDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InfoSupp'];

    function InfoSuppDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, InfoSupp) {
        var vm = this;

        vm.infoSupp = entity;
        vm.clear = clear;
        vm.save = save;
        vm.info = InfoSupp.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.infoSupp.id ) {
                InfoSupp.update({ id: vm.infoSupp.id }, vm.infoSupp, onSaveSuccess, onSaveError);
            } else {
                InfoSupp.save(vm.infoSupp, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:infoSuppUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
