(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Facture', Facture)
        .factory('FactureType', FactureType);

    Facture.$inject = ['$resource','BASE_URL_API'];
    FactureType.$inject = ['$resource','BASE_URL_API'];

    function Facture ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'factures/:id';

        return $resource(resourceUrl, {}, {
            'queryByFacturier': { method: 'GET',url:BASE_URL_API+'factures/of-facturier-optimiser/:id', isArray: true},
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
    function  FactureType($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'factures/Alls';
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
