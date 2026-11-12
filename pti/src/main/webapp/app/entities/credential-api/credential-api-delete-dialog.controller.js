(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CredentialApiDeleteController',CredentialApiDeleteController);

    CredentialApiDeleteController.$inject = ['$uibModalInstance', 'entity', 'CredentialApi'];

    function CredentialApiDeleteController($uibModalInstance, entity, CredentialApi) {
        var vm = this;

        vm.credentialApi = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (client_id) {
            console.log(client_id);
            CredentialApi.delete({id: vm.credentialApi.client_id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
