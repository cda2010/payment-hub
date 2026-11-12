(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Profil', Profil);

    Profil.$inject = ['$resource','BASE_URL_API'];

    function Profil ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'profils/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryOptimise': { method: 'GET',url:BASE_URL_API+'profils_optimise', isArray: true},
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
