(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('UserManagementDialogController',UserManagementDialogController);

    UserManagementDialogController.$inject = ['$stateParams', '$uibModalInstance', 'entity', 'User'];

    function UserManagementDialogController ($stateParams, $uibModalInstance, entity, User) {
        var vm = this;

        vm.authorities = ['ROLE_USER', 'ROLE_ADMIN','ROLE_CONSULT_TDB'];
        vm.clear = clear;
        vm.languages = null;
        vm.save = save;
        vm.user = entity;



        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function onSaveSuccess (result) {
            vm.isSaving = false;
            $uibModalInstance.close(result);
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        function save () {
            vm.isSaving = true;
            /*
              vm.registerAccount.langKey =  'en' ;
                vm.doNotMatch = null;
                vm.error = null;
                vm.errorUserExists = null;
                vm.errorEmailExists = null;*/
                        if (vm.user.id !== null) {
                            User.update({id:vm.user.id,login:vm.user.login,
                                firstName:vm.user.firstName,lastName:vm.user.lastName,
                                imageUrl:vm.user.imageUrl,
                                 langKey:vm.user.langKey,
                              authorities:vm.user.authorities
                            
                        }, onSaveSuccess, onSaveError);
                        } else {
                            vm.user.langKey = 'en';
                            User.save(vm.user, onSaveSuccess, onSaveError);
                        }
           /*
         if (vm.user.password !== vm.confirmPassword) {
                vm.doNotMatch = 'ERROR';
            } else {
               
            }    
           */
        }
    }
})();
