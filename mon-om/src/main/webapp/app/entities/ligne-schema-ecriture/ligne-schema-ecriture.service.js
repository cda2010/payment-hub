(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('LigneSchemaEcriture', LigneSchemaEcriture);

    LigneSchemaEcriture.$inject = ['$resource'];

    function LigneSchemaEcriture ($resource) {
        var resourceUrl =  'api/ligne-schema-ecritures/:id';

        return $resource(resourceUrl, {}, {
            'querySchema': { method: 'GET',url:'api/ligne-schema-ecritures/schema/:idschema' ,isArray: true},
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
