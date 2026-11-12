(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('PasswordResetFinish', PasswordResetFinish);

    PasswordResetFinish.$inject = ['$resource','BASE_URL_API'];

    function PasswordResetFinish($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'account/reset-password/finish', {}, {});

        return service;
    }
})();
