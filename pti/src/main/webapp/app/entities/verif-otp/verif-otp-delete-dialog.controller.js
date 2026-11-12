(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VerifOtpDeleteController',VerifOtpDeleteController);

    VerifOtpDeleteController.$inject = ['$uibModalInstance', 'entity', 'VerifOtp'];

    function VerifOtpDeleteController($uibModalInstance, entity, VerifOtp) {
        var vm = this;

        vm.verifOtp = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            VerifOtp.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
