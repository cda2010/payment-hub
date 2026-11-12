(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ProduitCanal', ProduitCanal);

    ProduitCanal.$inject = ['$resource'];

    function ProduitCanal ($resource) {
        var resourceUrl =  'api/produit-canals/:id';

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
