(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FtpDeleteController',FtpDeleteController);

    FtpDeleteController.$inject = ['$uibModalInstance', 'entity', 'Ftp'];

    function FtpDeleteController($uibModalInstance, entity, Ftp) {
        var vm = this;

        vm.ftp = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Ftp.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
