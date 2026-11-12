(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CompteAutre', CompteAutre);

    CompteAutre.$inject = ['$resource'];

    function CompteAutre ($resource) {
        var resourceUrl = 'api/v1/comptes-autres/:id';

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
