(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Ville', Ville);

    Ville.$inject = ['$resource','BASE_URL_API'];

    function Ville ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'villes/:id';

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
