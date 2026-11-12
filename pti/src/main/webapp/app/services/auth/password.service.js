(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('Password', Password)


    Password.$inject = ['$resource','BASE_URL_API'];

    function Password($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'account/change_password', {}, {});

        return service;

    }
})();
