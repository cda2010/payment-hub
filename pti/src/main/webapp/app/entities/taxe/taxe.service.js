(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Taxe', Taxe);

    Taxe.$inject = ['$resource','BASE_URL_API'];

    function Taxe ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'taxes/:id';

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
