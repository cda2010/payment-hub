(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeServiceReseauOrganisation', TypeServiceReseauOrganisation);

        TypeServiceReseauOrganisation.$inject = ['$resource'];

    function TypeServiceReseauOrganisation ($resource) { 
        var resourceUrl =  'api/type-service-reseau-organisation/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/type-service-reseau-organisation' ,isArray: true},
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
