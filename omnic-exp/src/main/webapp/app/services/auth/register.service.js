(function () {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .factory('Register', Register);

    Register.$inject = ['$resource'];

    function Register ($resource) {
        return $resource('api/register', {}, {});
    }
})();
