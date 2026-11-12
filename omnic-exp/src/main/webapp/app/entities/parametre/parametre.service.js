(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('Parametre', Parametre);

    Parametre.$inject = ['$resource'];

    function Parametre ($resource) {
        var resourceUrl =  'api/parametres/:id';

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
