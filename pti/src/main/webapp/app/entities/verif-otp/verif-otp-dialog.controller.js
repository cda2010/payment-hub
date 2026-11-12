(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VerifOtpDialogController', VerifOtpDialogController);

    VerifOtpDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'VerifOtp'];

    function VerifOtpDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, VerifOtp) {
        var vm = this;

        vm.verifOtp = entity;
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
            if (vm.verifOtp.id !== null) {
                VerifOtp.update(vm.verifOtp, onSaveSuccess, onSaveError);
            } else {
                VerifOtp.save(vm.verifOtp, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:verifOtpUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
