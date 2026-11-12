(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ClassificationFacturier', ClassificationFacturier);

    ClassificationFacturier.$inject = ['$resource','BASE_URL_API'];
    function ClassificationFacturier ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'classification-facturiers/:id';

        return $resource(resourceUrl, {}, {
            'queryFinalByUo': { method: 'GET',url:BASE_URL_API+'classification-facturiers-final/ofUo/:id', isArray: true},
            'queryByUo': { method: 'GET',url:BASE_URL_API+'classification-facturiers/ofUo/:id', isArray: true},
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
