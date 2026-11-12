(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Bouton', Bouton);

    Bouton.$inject = ['$resource'];

    function Bouton ($resource) {
        var resourceUrl =  'api/boutons/:id';

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
