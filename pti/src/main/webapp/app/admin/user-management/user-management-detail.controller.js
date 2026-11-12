(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserManagementDetailController', UserManagementDetailController);

    UserManagementDetailController.$inject = ['$stateParams', 'User','entity','InitPassword','$uibModalInstance','Notification'];

    function UserManagementDetailController($stateParams, User,entity,InitPassword,$uibModalInstance,Notification) {
        var vm = this;

        vm.user = entity;
        vm.clear = clear;
        vm.save = save;


        function onSaveSuccess (result) {
            vm.isSaving = false;
            $uibModalInstance.close(result);
            Notification.success('Mot de passe réinitialiser avec succès');
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function onSaveError () {
            vm.isSaving = false;
            Notification.success('Echec de l\'opération');
        }

        function save () {
            vm.isSaving = true;
            InitPassword.save(vm.user, onSaveSuccess, onSaveError);
        }
    }
})();
