(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Etiquette', Etiquette);

    Etiquette.$inject = ['$resource'];

    function Etiquette ($resource) {
        var resourceUrl =  'api/etiquettes/:id';

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
