(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('SchemaEcriture', SchemaEcriture);

    SchemaEcriture.$inject = ['$resource'];

    function SchemaEcriture ($resource) {
        var resourceUrl =  'api/schema-ecritures/:id';

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
