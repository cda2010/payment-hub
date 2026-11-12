(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Pays', Pays);

    Pays.$inject = ['$resource','BASE_URL_API'];

    function Pays ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'pays/:id';

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
