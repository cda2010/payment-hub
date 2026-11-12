(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ClientTypePage', ClientTypePage);

    ClientTypePage.$inject = ['$resource','BASE_URL_API'];

    function ClientTypePage ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'clients-page/type';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryPage': {method: 'GET', url: BASE_URL_API+'clients-page/type' ,isArray: true},
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
