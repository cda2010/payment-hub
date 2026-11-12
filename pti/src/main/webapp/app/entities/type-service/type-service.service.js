(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeService', TypeService);

    TypeService.$inject = ['$resource','BASE_URL_API'];

    function TypeService ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-services/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryAll': {method: 'GET', url: BASE_URL_API+'type-services_list', isArray: true},
            'queryTypeServiceByUserAuthorities': {method: 'GET', url: BASE_URL_API+'type-services/en-attente-par-role-validation/:login', isArray: true},
            'queryAllOptimise': {method: 'GET', url: BASE_URL_API+'type-services_list_optimise', isArray: true},
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
