(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Liste', Liste);

    Liste.$inject = ['$resource'];

    function Liste ($resource) {
        var resourceUrl =  'api/listes/:id';

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
