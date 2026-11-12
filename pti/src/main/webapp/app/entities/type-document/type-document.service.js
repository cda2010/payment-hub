(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeDocument', TypeDocument);

    TypeDocument.$inject = ['$resource'];

    function TypeDocument ($resource) {
        var resourceUrl =  'api/type-documents/:id';

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
