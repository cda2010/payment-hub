(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ClientCrea', ClientCrea);

    ClientCrea.$inject = ['$resource','BASE_URL_API'];

    function ClientCrea ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'clients/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
