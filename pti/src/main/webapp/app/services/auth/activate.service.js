(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('Activate', Activate);

    Activate.$inject = ['$resource','BASE_URL_API'];

    function Activate ($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'activate', {}, {
            'get': { method: 'GET', params: {}, isArray: false}
        });

        return service;
    }
})();
