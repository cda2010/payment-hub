(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ListeDiffusion', ListeDiffusion);

    ListeDiffusion.$inject = ['$resource','BASE_URL_API'];

    function ListeDiffusion ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'liste-diffusions/:id';

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
