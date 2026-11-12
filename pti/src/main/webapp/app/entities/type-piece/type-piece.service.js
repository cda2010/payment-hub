(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypePiece', TypePiece);

    TypePiece.$inject = ['$resource','BASE_URL_API'];

    function TypePiece ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-pieces/:id';

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
