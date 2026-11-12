(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Produit', Produit);

    Produit.$inject = ['$resource','BASE_URL_API'];

    function Produit ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'produits/:id';

        return $resource(resourceUrl, {}, {
            'queryByFacturier': { method: 'GET',url:BASE_URL_API+'produits/ofFacturier/:id', isArray: true},
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
