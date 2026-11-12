(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('TypeCompte', TypeCompte);

    TypeCompte.$inject = ['$resource'];

    function TypeCompte ($resource) {
        var resourceUrl =  'api/type-comptes/:id';

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
