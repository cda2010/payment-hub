(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChangeEtatConfirmController',ChangeEtatConfirmController);

    ChangeEtatConfirmController.$inject = ['$uibModalInstance'];

    function ChangeEtatConfirmController($uibModalInstance) {
        var vm = this;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete () {
            $uibModalInstance.close(true);
        }
    }
})();
