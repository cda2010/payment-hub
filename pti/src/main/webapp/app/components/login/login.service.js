(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('LoginService', LoginService);

    LoginService.$inject = ['$uibModal','$state'];

    function LoginService ($uibModal,$state) {
        var service = {
            open: open
        };

        var modalInstance = null;
        var resetModal = function () {
            modalInstance = null;
        };

        return service;

        function open () {
            if (modalInstance !== null) return;
            $state.go("login");
        }
    }
})();
