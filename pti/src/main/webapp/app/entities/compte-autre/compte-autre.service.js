(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CompteAutre', CompteAutre);

    CompteAutre.$inject = ['$resource','BASE_URL_API'];

    function CompteAutre ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'compte-autres/:id';

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
