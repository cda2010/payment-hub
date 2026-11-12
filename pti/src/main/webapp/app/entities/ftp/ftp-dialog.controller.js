(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FtpDialogController', FtpDialogController);

    FtpDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Ftp'];

    function FtpDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Ftp) {
        var vm = this;

        vm.ftp = entity;
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
            if (vm.ftp.id !== null) {
                Ftp.update(vm.ftp, onSaveSuccess, onSaveError);
            } else {
                Ftp.save(vm.ftp, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ftpUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
