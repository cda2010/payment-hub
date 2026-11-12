(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PasswordController', PasswordController);

    PasswordController.$inject = ['Auth', 'Principal'];

    function PasswordController (Auth, Principal) {
        var vm = this;

        vm.changePassword = changePassword;
        vm.error = null;
        vm.success = null;
        vm.password = {};

        Principal.identity().then(function(account) {
            vm.account = account;
        });

        function changePassword () {
                Auth.changePassword(vm.password).then(function () {
                    vm.error = null;
                    vm.success = 'OK';
                }).catch(function () {
                    vm.success = null;
                    vm.error = 'ERROR';
                });
            }
    }
})();
