(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Ecran', Ecran);

    Ecran.$inject = ['$resource','BASE_URL_API'];

    function Ecran ($resource, BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'ecrans/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByProduit': { method: 'GET', url:BASE_URL_API+"ecrans/of-produit/:id",isArray: true},
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
