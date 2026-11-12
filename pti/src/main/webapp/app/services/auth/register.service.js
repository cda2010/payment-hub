(function () {
    'use strict';

    angular
        .module('ptiApp')
        .factory('Register', Register);

    Register.$inject = ['$resource','BASE_URL_API'];

    function Register ($resource,BASE_URL_API) {
        return $resource(BASE_URL_API+'register', {}, {});
    }
})();
