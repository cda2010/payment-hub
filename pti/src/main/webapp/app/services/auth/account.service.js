(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('Account', Account);

    Account.$inject = ['$resource','BASE_URL_API'];

    function Account ($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'account', {}, {
            'get': { method: 'GET', params: {}, isArray: false,
                interceptor: {
                    response: function(response) {
                        // expose response
                        return response;
                    }
                }
            }
        });

        return service;
    }
})();
