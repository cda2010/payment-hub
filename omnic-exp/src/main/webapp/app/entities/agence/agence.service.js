(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('Agence', Agence);

    Agence.$inject = ['$resource'];

    function Agence ($resource) {
        var resourceUrl =  'api/agences/:id';

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
