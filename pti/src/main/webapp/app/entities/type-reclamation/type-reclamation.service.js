(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeReclamation',TypeReclamation);

    TypeReclamation.$inject = ['$resource','BASE_URL_API'];

    function TypeReclamation ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-reclamations/:id';

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
