(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Autorisation', Autorisation)
        .factory('AutorisationByUO', AutorisationByUO);

    Autorisation.$inject = ['$resource','BASE_URL_API'];
    AutorisationByUO.$inject = ['$resource','BASE_URL_API'];

    function Autorisation ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'autorisations/:id';
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

    function AutorisationByUO ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'autorisationsByUO/:uoId';
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
            }
        });
    }
})();
