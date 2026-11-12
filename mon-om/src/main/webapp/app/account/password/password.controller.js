(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .controller('PasswordController', PasswordController);

    PasswordController.$inject = ['Auth', 'Principal'];

    function PasswordController(Auth, Principal) {
        var vm = this;

        vm.changePassword = changePassword;
        vm.doNotMatch = null;
        vm.error = null;
        vm.success = null;

        Principal.identity().then(function (account) {
            vm.account = account;
            console.log(vm.account);
        });

        function changePassword() {

            Auth.login({
                username: vm.account.login,
                password: vm.oldpassword,
                rememberMe: false
            }).then(function () {
                console.log("OK");
                vm.authenticationError = false;
                if (vm.password !== vm.confirmPassword) {
                    vm.error = null;
                    vm.success = null;
                    vm.doNotMatch = 'ERROR';
                } else {
                    vm.doNotMatch = null;
                    Auth.changePassword(vm.password).then(function () {
                        vm.error = null;
                        vm.success = 'OK';
                    }).catch(function () {
                        vm.success = null;
                        vm.error = 'ERROR';
                    });
                }


            }).catch(function () {
                vm.authenticationError = true;
            });



        }
    }
})();
