(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeUniteOrganisation', TypeUniteOrganisation)
        .factory('TypeUOWithoutUR', TypeUOWithoutUR);

    TypeUniteOrganisation.$inject = ['$resource','BASE_URL_API'];
    TypeUOWithoutUR.$inject = ['$resource','BASE_URL_API'];

    function TypeUniteOrganisation ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-unite-organisations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryOptimisetUo': { method: 'GET', url:BASE_URL_API+'type-unite-organisations-optimise',isArray: true},
            'queryList':{method: 'GET',url:BASE_URL_API+'type-unite-organisationsWithoutUR' ,isArray: true},
            'getOptimise':{method: 'GET',url:BASE_URL_API+'type-unite-organisations-optimise/:id' ,isArray: false},
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
    function TypeUOWithoutUR ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'type-unite-organisationsWithoutUR';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'optimise':{method: 'GET',url:BASE_URL_API+'type-unite-organisationsWithoutUR-optimise' ,isArray: true},
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
