(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('PointVente', PointVente)
        .factory('PointVenteByUO', PointVenteByUO);

    PointVente.$inject = ['$resource','BASE_URL_API'];
    PointVenteByUO.$inject = ['$resource','BASE_URL_API'];

    function PointVente ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'point-ventes/:id';

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

    function PointVenteByUO ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'point-ventesByUO/:uoId';

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
