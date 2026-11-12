(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('PasswordResetInit', PasswordResetInit);

    PasswordResetInit.$inject = ['$resource','BASE_URL_API'];

    function PasswordResetInit($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'account/reset_password/init', {}, {});

        return service;
    }
})();
