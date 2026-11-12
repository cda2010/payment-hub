(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('ParametreSchema', ParametreSchema);

    ParametreSchema.$inject = ['$resource'];

    function ParametreSchema ($resource) {
        var resourceUrl =  'api/parametre-schemas/:id';

        return $resource(resourceUrl, {}, {
            'queryLigne': { method: 'GET', url:'api/parametre-schemas/ligne/:idligne', isArray: true},
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
