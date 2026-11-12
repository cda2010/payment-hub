(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeCompte', TypeCompte);

    TypeCompte.$inject = ['$resource','BASE_URL_API'];

    function TypeCompte ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-comptes/:id';

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
